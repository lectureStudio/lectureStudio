package org.lecturestudio.web.api.model;

/**
 * Encapsulates the profile data for a user.
 *
 * @author Alex Andres
 */

public class UserProfile {
    /**
     * The display name of a user.
     */
    private String displayname;

    /**
     * The avatar URL for a user.
     */
    private String avatarUrl;


    /**
     * @return The user's display name.
     */
    public String getDisplayName() {
        return displayname;
    }

    /**
     * @return The user's avatar URL.
     */
    public String getAvatarUrl() {
        return avatarUrl;
    }

    @Override
    public String toString() {
        return "UserProfile [displayname=" + displayname + ", avatarUrl="
                + avatarUrl + "]";
    }
}
