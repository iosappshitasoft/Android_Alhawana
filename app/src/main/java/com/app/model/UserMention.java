package com.app.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hendraanggrian.appcompat.socialview.autocomplete.Mentionable;

//import com.hendraanggrian.appcompat.widget.Mentionable;

public class UserMention implements Mentionable  {
    private final CharSequence userName;
    private final CharSequence displayName;
    private final CharSequence userId;
    private final Object avatar;

    public UserMention(@NonNull CharSequence userName) {
        this(userName, null, null, null);
    }

    public UserMention(@NonNull CharSequence userName, @Nullable CharSequence displayName) {
        this(null, userName, displayName, null);
    }

    public UserMention(@NonNull CharSequence userId, @NonNull CharSequence userName, @Nullable CharSequence displayName, @Nullable Object avatar) {
        this.userId = userId;
        this.userName = userName;
        this.displayName = displayName;
        this.avatar = avatar;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof UserMention && ((UserMention) obj).userName == userName;
    }

    @Override
    public int hashCode() {
        return userName.hashCode();
    }

    @NonNull
    @Override
    public String toString() {
        return userName.toString();
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    public CharSequence getUserId() {
        return userId;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    public CharSequence getUsername() {
        return userName;
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    public CharSequence getDisplayname() {
        return displayName;
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    public Object getAvatar() {
        return avatar;
    }
}