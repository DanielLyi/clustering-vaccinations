package com.danylo.visual;

import java.awt.*;

public class GBC extends GridBagConstraints {


    public GBC(int gridX, int gridY){
        super.gridx = gridX;
        super.gridy = gridY;
    }

    public GBC(int gridX, int gridY, int gridWidth, int gridHeight){
        super.gridx = gridX;
        super.gridy = gridY;
        super.gridwidth = gridWidth;
        super.gridheight = gridHeight;
    }

    public GBC setAnchor(int anchor){
        super.anchor = anchor;
        return this;
    }

    public GBC setWeights(double weightX, double weightY){
        super.weightx = weightX;
        super.weighty = weightY;
        return this;
    }

    public GBC setFill(int fill){
        super.fill = fill;
        return this;
    }

    public GBC setInsets(int k){
        this.insets = new Insets(k,k,k,k);
        return this;
    }

}
