package com.fullarmor.hapi;

/*****************************************************************
 <copyright file="OnHandleFileListener.java" company="FullArmor Corp.">
 Copyright (c) 2015 FullArmor Corporation. All rights reserved.
 </copyright>
 *****************************************************************/

public interface OnHandleFileListener {
    /**
     * This method is called after clicking the Save or Load button on the
     * dialog, if the file name was correct. It should be used to save or load a
     * file using the filePath path.
     *
     * @param filePath
     *            File path set in the dialog when the Save or Load button was
     *            clicked.
     */
    void handleFile(String filePath);
}
