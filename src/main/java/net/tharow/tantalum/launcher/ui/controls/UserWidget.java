/*
 * This file is part of The Technic Launcher Version 3.
 * Copyright ©2015 Syndicate, LLC
 *
 * The Technic Launcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Technic Launcher  is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Technic Launcher.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.tharow.tantalum.launcher.ui.controls;

import net.tharow.tantalum.ui.lang.ResourceLoader;
import net.tharow.tantalum.launcher.ui.LauncherFrame;
import net.tharow.tantalum.launchercore.auth.IUserType;
import net.tharow.tantalum.launchercore.auth.TantalumUser;
import net.tharow.tantalum.launchercore.image.IImageJobListener;
import net.tharow.tantalum.launchercore.image.ImageJob;
import net.tharow.tantalum.launchercore.image.ImageRepository;
import net.tharow.tantalum.utilslib.ImageUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class UserWidget extends JPanel implements IImageJobListener<TantalumUser> {

    private ImageRepository<IUserType> skinRepository;

    private JLabel userName;
    private JLabel avatar;
    private IUserType currentMojangUser;

    public UserWidget(ResourceLoader resources, ImageRepository<IUserType> skinRepository) {
        this.skinRepository = skinRepository;

        initComponents(resources);
    }

    private void initComponents(ResourceLoader resources) {
        setOpaque(false);

        avatar = new JLabel();
        avatar.setIcon(resources.getIcon("news/authorHelm.png"));
        this.add(avatar);

        String fullText = resources.getString("launcher.user.logged");

        int endPreText = fullText.indexOf("{0}");
        int startPostText = endPreText + 3;
        String preText = "";
        String postText = "";

        if (endPreText < 0) {
            preText = fullText;
        } else {
            if (endPreText == 0) {
                preText = "";
            } else {
                preText = fullText.substring(0, endPreText);
            }

            if (startPostText >= fullText.length()) {
                postText = "";
            } else {
                postText = fullText.substring(startPostText);
            }
        }

        JLabel staticText = new JLabel(preText);
        staticText.setForeground(LauncherFrame.COLOR_WHITE_TEXT);
        staticText.setFont(resources.getFont(ResourceLoader.FONT_RALEWAY, 15));

        if (preText.length() > 0)
            this.add(staticText);

        userName = new JLabel("");
        userName.setForeground(LauncherFrame.COLOR_WHITE_TEXT);
        userName.setBackground(Color.white);
        userName.setFont(resources.getFont(ResourceLoader.FONT_RALEWAY, 17, Font.BOLD));
        this.add(userName);

        staticText = new JLabel(postText);
        staticText.setForeground(LauncherFrame.COLOR_WHITE_TEXT);
        staticText.setFont(resources.getFont(ResourceLoader.FONT_RALEWAY, 15));

        if (postText.length() > 0)
            this.add(staticText);
    }

    public void setUser(IUserType mojangUser) {
        currentMojangUser = mojangUser;
        userName.setText(mojangUser.getDisplayName());

        ImageJob<IUserType> job = skinRepository.startImageJob(currentMojangUser);
        job.addJobListener(this);
        refreshFace(job.getImage());
    }

    private void refreshFace(BufferedImage image) {
        avatar.setIcon(new ImageIcon(ImageUtils.scaleWithAspectWidth(image, 30)));
    }

    @Override
    public void jobComplete(ImageJob<TantalumUser> job) {
        if (job.getJobData() == currentMojangUser)
            refreshFace(job.getImage());
    }
}
