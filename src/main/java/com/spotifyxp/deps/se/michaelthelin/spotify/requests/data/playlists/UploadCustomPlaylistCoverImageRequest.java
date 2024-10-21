package com.spotifyxp.deps.se.michaelthelin.spotify.requests.data.playlists;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.spotifyxp.deps.se.michaelthelin.spotify.requests.data.AbstractDataRequest;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import java.io.IOException;

/**
 * Replace the image used to represent a specific playlist.
 */
@JsonDeserialize(builder = UploadCustomPlaylistCoverImageRequest.Builder.class)
public class UploadCustomPlaylistCoverImageRequest extends AbstractDataRequest<String> {

    /**
     * The private {@link UploadCustomPlaylistCoverImageRequest} constructor.
     *
     * @param builder A {@link UploadCustomPlaylistCoverImageRequest.Builder}.
     */
    private UploadCustomPlaylistCoverImageRequest(final Builder builder) {
        super(builder);
    }

    /**
     * Upload a new playlist image.
     *
     * @return A string. <b>Note:</b> This endpoint doesn't return something in its response body.
     * @throws IOException In case of networking issues.
     */
    public String execute() throws
            IOException {
        return putJson();
    }

    /**
     * Builder class for building an {@link UploadCustomPlaylistCoverImageRequest}.
     */
    public static final class Builder extends AbstractDataRequest.Builder<String, Builder> {

        /**
         * Create a new {@link UploadCustomPlaylistCoverImageRequest.Builder}.
         * <p>
         * This access token must be tied to the user who owns the playlist, and must have the scope
         * {@code ugc-image-upload} granted. In addition, the token must also contain {@code playlist-modify-public}
         * and/or {@code playlist-modify-private}, depending the public status of the playlist you want to update.
         *
         * @param accessToken Required. A valid access token from the Spotify Accounts service.
         * @see <a href="https://developer.spotify.com/web-api/using-scopes/">Spotify: Using Scopes</a>
         */
        public Builder(final String accessToken) {
            super(accessToken);
        }

        /**
         * The playlist ID setter.
         *
         * @param playlist_id The Spotify ID for the playlist.
         * @return A {@link UploadCustomPlaylistCoverImageRequest.Builder}.
         * @see <a href="https://developer.spotify.com/web-api/user-guide/#spotify-uris-and-ids">Spotify: URIs &amp; IDs</a>
         */
        public Builder playlist_id(final String playlist_id) {
            assert (playlist_id != null);
            assert (!playlist_id.isEmpty());
            return setPathParameter("playlist_id", playlist_id);
        }

        /**
         * The image data setter.
         *
         * @param image_data Base64 encoded JPEG image data, maximum payload size is 256 KB.
         * @return A {@link UploadCustomPlaylistCoverImageRequest.Builder}.
         */
        public Builder image_data(final String image_data) {
            assert (image_data != null);
            assert (!image_data.isEmpty());
            assert (image_data.getBytes().length <= 256000);
            return setBody(RequestBody.create(image_data, MediaType.parse("image/jpeg")));
        }

        /**
         * The request build method.
         *
         * @return A custom {@link UploadCustomPlaylistCoverImageRequest}.
         */
        @Override
        public UploadCustomPlaylistCoverImageRequest build() {
            setContentType("image/jpeg");
            setPath("/v1/playlists/{playlist_id}/images");
            return new UploadCustomPlaylistCoverImageRequest(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
