package com.app.Zego.internal.business.pk;

import java.util.ArrayList;
import java.util.List;

import im.zego.zegoexpress.entity.ZegoMixerInput;
import im.zego.zegoexpress.entity.ZegoMixerVideoConfig;

public interface MixLayoutProvider {

    ArrayList<ZegoMixerInput> getMixVideoInputs(List<String> streamList, ZegoMixerVideoConfig videoConfig);
}
