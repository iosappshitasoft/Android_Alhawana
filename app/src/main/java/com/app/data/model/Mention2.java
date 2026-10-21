package com.app.data.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hendraanggrian.appcompat.socialview.autocomplete.Mentionable;


public class Mention2 implements Mentionable {

    private final String id;
    private final CharSequence username;
    private final CharSequence displayname;
    private final Object avatar;

    public Mention2(@NonNull String id, @NonNull CharSequence username) {
        this(id, username, null);
    }

    public Mention2(@NonNull String id, @NonNull CharSequence username, @Nullable CharSequence displayname) {
        this(id, username, displayname, null);
    }

    public Mention2(@NonNull String id, @NonNull CharSequence username, @Nullable CharSequence displayname, @Nullable Object avatar) {
        this.username = username;
        this.displayname = displayname;
        this.avatar = avatar;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof Mention2 && ((Mention2) obj).username.equals(username);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }

    @NonNull
    @Override
    public String toString() {
        return username.toString();
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public CharSequence getUsername() {
        return username;
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public CharSequence getDisplayname() {
        return displayname;
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public Object getAvatar() {
        return avatar;
    }
}