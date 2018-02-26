package com.github.pockethub.android.tests.gist;


import android.test.AndroidTestCase;

import com.github.pockethub.android.ui.item.gist.GistItem;
import com.github.pockethub.android.util.AvatarLoader;
import com.meisolsson.githubsdk.model.Gist;
import com.meisolsson.githubsdk.model.User;

public class GistItemTest extends AndroidTestCase {

    public void testGroupieGistItemPosition() {

        User user = User.builder()
                .name("user")
                .build();

        Gist gist = Gist.builder()
                .owner(user)
                .id("gistid")
                .build();

        AvatarLoader loader = new AvatarLoader(getContext());
        GistItem item = new GistItem(loader, gist);
        GistItem otherItem = new GistItem(loader, gist);

        assertEquals(0, item.getPosition(otherItem));
    }
}
