package com.spotifyxp.panels;

import com.spotifyxp.PublicValues;
import com.spotifyxp.api.UnofficialSpotifyAPI;
import com.spotifyxp.custom.StoppableThreadRunnable;
import com.spotifyxp.deps.se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import com.spotifyxp.deps.se.michaelthelin.spotify.model_objects.specification.Artist;
import com.spotifyxp.deps.se.michaelthelin.spotify.model_objects.specification.Track;
import com.spotifyxp.lib.libLanguage;
import com.spotifyxp.logging.ConsoleLogging;
import com.spotifyxp.threading.StoppableThread;
import com.spotifyxp.utils.TrackUtils;
import org.apache.hc.core5.http.ParseException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class HomePanel {
    JScrollPane scrollholder;
    JPanel content;

    UnofficialSpotifyAPI.HomeTab tab;

    public HomePanel() {
        tab = new UnofficialSpotifyAPI(ContentPanel.api.getSpotifyApi().getAccessToken()).getHomeTab();
        initializeLayout();
    }

    public void initializeLayout() {
        content = new JPanel();
        content.setPreferredSize(new Dimension(784, 337 * tab.sections.size()));
        scrollholder = new JScrollPane(content);
        scrollholder.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollholder.setSize(784, 421);
        Thread t = new Thread(this::initializeContent);
        t.start();
    }

    int addCache = 302;

    int cache = 0;

    enum ContentTypes {
        show,
        track,
        album,
        artist,
        user,
        playlist
    }

    public void addModule(UnofficialSpotifyAPI.HomeTabSection section) {
        ArrayList<String> uricache = new ArrayList<>();
        JLabel homepanelmoduletext = new JLabel(section.name);
        homepanelmoduletext.setFont(new Font("Tahoma", Font.PLAIN, 16));
        homepanelmoduletext.setBounds(0, addCache + 11, 375, 24);
        content.add(homepanelmoduletext);

        JScrollPane homepanelmodulescrollpanel = new JScrollPane();
        homepanelmodulescrollpanel.setBounds(0, addCache + 38, 777, 281);
        content.add(homepanelmodulescrollpanel);

        JTable homepanelmodulecontenttable = new JTable()  {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        homepanelmodulescrollpanel.setViewportView(homepanelmodulecontenttable);

        homepanelmodulecontenttable.setModel(new DefaultTableModel(
                new Object[][] {
                },
                new String[] {
                        "Name", "Artist"
                }
        ));


        for(UnofficialSpotifyAPI.HomeTabAlbum album : section.albums) {
            uricache.add(album.uri);
            ((DefaultTableModel) homepanelmodulecontenttable.getModel()).addRow(new Object[]{album.name, artistParser(album.artists)});
        }
        for(UnofficialSpotifyAPI.HomeTabEpisodeOrChapter episodeOrChapter : section.episodeOrChapters) {
            uricache.add(episodeOrChapter.uri);
            ((DefaultTableModel) homepanelmodulecontenttable.getModel()).addRow(new Object[]{episodeOrChapter.EpisodeOrChapterName, episodeOrChapter.name + " - " + episodeOrChapter.publisherName});
        }
        for(UnofficialSpotifyAPI.HomeTabPlaylist playlist : section.playlists) {
            uricache.add(playlist.uri);
            ((DefaultTableModel) homepanelmodulecontenttable.getModel()).addRow(new Object[]{playlist.name, playlist.ownerName});
        }
        for(UnofficialSpotifyAPI.HomeTabArtist artist : section.artists) {
            uricache.add(artist.uri);
            ((DefaultTableModel) homepanelmodulecontenttable.getModel()).addRow(new Object[]{artist.name, ""});
        }

        homepanelmodulecontenttable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(e.getClickCount() == 2) {
                    ContentTypes ct = ContentTypes.valueOf(uricache.get(homepanelmodulecontenttable.getSelectedRow()).split(":")[1]);
                    String uri = uricache.get(homepanelmodulecontenttable.getSelectedRow());
                    String id = uri.split(":")[2];
                    try {
                        switch (ct) {
                            case track:
                                PublicValues.spotifyplayer.load(uri, true, false);
                                break;
                            case artist:
                                scrollholder.setVisible(false);
                                ContentPanel.artistPanel.artistpopularuricache.clear();
                                ContentPanel.artistPanel.artistalbumuricache.clear();
                                ((DefaultTableModel)ContentPanel.artistPanel.artistalbumalbumtable.getModel()).setRowCount(0);
                                ((DefaultTableModel)ContentPanel.artistPanel.artistpopularsonglist.getModel()).setRowCount(0);
                                ContentPanel.artistPanel.artisttitle.setText("");
                                try {
                                    Artist a = ContentPanel.api.getSpotifyApi().getArtist(id).build().execute();
                                    try {
                                        ContentPanel.artistPanel.artistimage.setImage(new URL(a.getImages()[0].getUrl()).openStream());
                                    } catch (ArrayIndexOutOfBoundsException exception) {
                                        //No artist image (when this is raised it's a bug)
                                    }
                                    ContentPanel.artistPanel.artisttitle.setText(a.getName());
                                    StoppableThread trackthread = new StoppableThread(counter -> {
                                        try {
                                            for (Track t : ContentPanel.api.getSpotifyApi().getArtistsTopTracks(id, ContentPanel.countryCode).build().execute()) {
                                                ContentPanel.artistPanel.artistpopularuricache.add(t.getUri());
                                                ContentPanel.api.addSongToList(TrackUtils.getArtists(t.getArtists()), t, ContentPanel.artistPanel.artistpopularsonglist);
                                            }
                                        } catch (IOException | ParseException | SpotifyWebApiException ex) {
                                            ConsoleLogging.Throwable(ex);
                                        }
                                    },false);
                                    StoppableThread albumthread = new StoppableThread(counter -> ContentPanel.api.addAllAlbumsToList(ContentPanel.artistPanel.artistalbumuricache, uri, ContentPanel.artistPanel.artistalbumalbumtable), false);
                                    albumthread.start();
                                    trackthread.start();
                                } catch (IOException | ParseException | SpotifyWebApiException ex) {
                                    ConsoleLogging.Throwable(ex);
                                }
                                ContentPanel.artistPanel.contentPanel.setVisible(true);
                                ContentPanel.artistPanelBackButton.setVisible(true);
                                break;
                            default:
                                ContentPanel.showAdvancedSongPanel(uri, ct);
                                break;
                        }
                    }catch (Exception ignored) {
                    }
                }
            }
        });

        addCache+=319;
        cache++;
        content.revalidate();
        content.repaint();
    }

    String artistParser(ArrayList<UnofficialSpotifyAPI.HomeTabArtistNoImage> cache) {
        StringBuilder builder = new StringBuilder();
        int read = 0;
        for(UnofficialSpotifyAPI.HomeTabArtistNoImage s : cache) {
            if(read==cache.size()) {
                builder.append(s.name);
            }else{
                builder.append(s.name).append(",");
            }
            read++;
        }
        return builder.toString();
    }

    public void initializeContent() {
        content.setLayout(null);
        ArrayList<String> usersuricache = new ArrayList<>();

        JScrollPane homepaneluserscrollpanel = new JScrollPane();
        homepaneluserscrollpanel.setBounds(0, 39, 777, 261);
        content.add(homepaneluserscrollpanel);

        JTable homepanelusertable = new JTable()  {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        homepaneluserscrollpanel.setViewportView(homepanelusertable);

        homepanelusertable.setModel(new DefaultTableModel(
                new Object[][] {
                },
                new String[] {
                        "Name", "Artist"
                }
        ));

        homepanelusertable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(e.getClickCount() == 2) {
                    ContentTypes ct = ContentTypes.valueOf(usersuricache.get(homepanelusertable.getSelectedRow()).split(":")[1]);
                    String uri = usersuricache.get(homepanelusertable.getSelectedRow());
                    String id = uri.split(":")[2];
                    try {
                        switch (ct) {
                            case track:
                                PublicValues.spotifyplayer.load(uri, true, false);
                                break;
                            case artist:
                                scrollholder.setVisible(false);
                                ContentPanel.artistPanel.artistpopularuricache.clear();
                                ContentPanel.artistPanel.artistalbumuricache.clear();
                                ((DefaultTableModel)ContentPanel.artistPanel.artistalbumalbumtable.getModel()).setRowCount(0);
                                ((DefaultTableModel)ContentPanel.artistPanel.artistpopularsonglist.getModel()).setRowCount(0);
                                ContentPanel.artistPanel.artisttitle.setText("");
                                try {
                                    Artist a = ContentPanel.api.getSpotifyApi().getArtist(id).build().execute();
                                    try {
                                        ContentPanel.artistPanel.artistimage.setImage(new URL(a.getImages()[0].getUrl()).openStream());
                                    } catch (ArrayIndexOutOfBoundsException exception) {
                                        //No artist image (when this is raised it's a bug)
                                    }
                                    ContentPanel.artistPanel.artisttitle.setText(a.getName());
                                    StoppableThread trackthread = new StoppableThread(counter -> {
                                        try {
                                            for (Track t : ContentPanel.api.getSpotifyApi().getArtistsTopTracks(id, ContentPanel.countryCode).build().execute()) {
                                                ContentPanel.artistPanel.artistpopularuricache.add(t.getUri());
                                                ContentPanel.api.addSongToList(TrackUtils.getArtists(t.getArtists()), t, ContentPanel.artistPanel.artistpopularsonglist);
                                            }
                                        } catch (IOException | ParseException | SpotifyWebApiException ex) {
                                            ConsoleLogging.Throwable(ex);
                                        }
                                    },false);
                                    StoppableThread albumthread = new StoppableThread(counter -> ContentPanel.api.addAllAlbumsToList(ContentPanel.artistPanel.artistalbumuricache, uri, ContentPanel.artistPanel.artistalbumalbumtable), false);
                                    albumthread.start();
                                    trackthread.start();
                                } catch (IOException | ParseException | SpotifyWebApiException ex) {
                                    ConsoleLogging.Throwable(ex);
                                }
                                ContentPanel.artistPanel.contentPanel.setVisible(true);
                                ContentPanel.artistPanelBackButton.setVisible(true);
                                break;
                            default:
                                ContentPanel.showAdvancedSongPanel(uri, ct);
                                break;
                        }
                    }catch (Exception ignored) {
                    }
                }
            }
        });

        JLabel homepanelgreetingstext = new JLabel(tab.greeting);
        homepanelgreetingstext.setFont(new Font("Tahoma", Font.PLAIN, 16));
        homepanelgreetingstext.setBounds(0, 11, 375, 24);
        content.add(homepanelgreetingstext);

        for(UnofficialSpotifyAPI.HomeTabAlbum album : tab.firstSection.albums) {
            usersuricache.add(album.uri);
            ((DefaultTableModel) homepanelusertable.getModel()).addRow(new Object[]{album.name, artistParser(album.artists)});
        }
        for(UnofficialSpotifyAPI.HomeTabEpisodeOrChapter episodeOrChapter : tab.firstSection.episodeOrChapters) {
            usersuricache.add(episodeOrChapter.uri);
            ((DefaultTableModel) homepanelusertable.getModel()).addRow(new Object[]{episodeOrChapter.EpisodeOrChapterName, episodeOrChapter.name + " - " + episodeOrChapter.publisherName});
        }
        for(UnofficialSpotifyAPI.HomeTabPlaylist playlist : tab.firstSection.playlists) {
            usersuricache.add(playlist.uri);
            ((DefaultTableModel) homepanelusertable.getModel()).addRow(new Object[]{playlist.name, playlist.ownerName});
        }
        for(UnofficialSpotifyAPI.HomeTabArtist artist : tab.firstSection.artists) {
            usersuricache.add(artist.uri);
            ((DefaultTableModel) homepanelusertable.getModel()).addRow(new Object[]{artist.name, ""});
        }

        for(UnofficialSpotifyAPI.HomeTabSection section : tab.sections) {
            addModule(section);
        }

        libLanguage l = PublicValues.language;
    }


    public JScrollPane getComponent() {
        return scrollholder;
    }

    public JPanel getPanel() {
        return content;
    }
}