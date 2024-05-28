package com.spotifyxp.api;

import com.spotifyxp.deps.se.michaelthelin.spotify.SpotifyApi;
import com.spotifyxp.deps.se.michaelthelin.spotify.model_objects.specification.AlbumSimplified;
import com.spotifyxp.deps.se.michaelthelin.spotify.model_objects.specification.Artist;
import com.spotifyxp.deps.se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import com.spotifyxp.deps.se.michaelthelin.spotify.model_objects.specification.Track;
import com.spotifyxp.manager.InstanceManager;
import com.spotifyxp.threading.DefThread;
import com.spotifyxp.utils.TrackUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;

@SuppressWarnings("CanBeFinal")
public class SpotifyAPI {
    static SpotifyApi spotifyApi = null;

    /**
     * Injects a SpotifyApi instance (For debugging)
     * @param api an instance of the class SpotifyApi
     * @see SpotifyApi
     */
    public void setSpotifyApi(SpotifyApi api) {
        spotifyApi = api;
    }

    /**
     * Adds all albums to the table specified
     * @param uricache cache that holds all uris
     * @param fromuri artist uri
     * @param totable the table to store all albums found
     */
    public void addAllAlbumsToList(ArrayList<String> uricache, String fromuri, JTable totable) {
        DefThread thread = new DefThread(() -> {
            try {
                int offset = 0;
                int limit = 50;
                int parsed = 0;
                int total = InstanceManager.getSpotifyApi().getArtistsAlbums(fromuri.split(":")[2]).build().execute().getTotal();
                int counter = 0;
                int last = 0;
                while(parsed != total) {
                    for(AlbumSimplified album : InstanceManager.getSpotifyApi().getArtistsAlbums(fromuri.split(":")[2]).offset(offset).limit(limit).build().execute().getItems()) {
                        ((DefaultTableModel) totable.getModel()).addRow(new Object[]{album.getName()});
                        uricache.add(album.getUri());
                        parsed++;
                    }
                    if(last == parsed) {
                        if(counter > 1) {
                            break;
                        }
                        counter++;
                    }else{
                        counter = 0;
                    }
                    last = parsed;
                    offset += limit;
                }
            }catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();
    }

    /**
     * Adds an album to a table
     * @param simplified instance of AlbumSimplified
     * @param table table to store the album
     * @see AlbumSimplified
     */
    public void addAlbumToList(AlbumSimplified simplified, JTable table) {
        ((DefaultTableModel) table.getModel()).addRow(new Object[] {simplified.getName()});
    }

    /**
     * Adds an album to a table
     * @param artist instance of Artist
     * @param table table to store the album
     * @see Artist
     */
    public void addArtistToList(Artist artist, JTable table) {
        ((DefaultTableModel) table.getModel()).addRow(new Object[]{artist.getName()});
    }

    /**
     * Adds a song to a table
     * @param artists artists to insert (for display)
     * @param track instance of Track
     * @param table table to store the song
     * @see Track
     */
    public void addSongToList(String artists, Track track, JTable table) {
        ((DefaultTableModel) table.getModel()).addRow(new Object[]{track.getName() + " - " + track.getAlbum().getName() + " - " + artists, TrackUtils.calculateFileSizeKb(track), TrackUtils.getBitrate(),TrackUtils.getHHMMSSOfTrack(track.getDurationMs())});
    }

    /**
     * Adds a playlist to a table
     * @param simplified instance of PlaylistSimplified
     * @param table table to store the playlist
     * @see PlaylistSimplified
     */
    public void addPlaylistToList(PlaylistSimplified simplified, JTable table) {
        ((DefaultTableModel) table.getModel()).addRow(new Object[]{simplified.getName() + " - " + simplified.getOwner().getDisplayName()});
    }
}
