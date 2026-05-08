package com.evandev.spicedcider.mixin.compat;

import com.bawnorton.mixinsquared.api.MixinCanceller;

import java.util.List;

public class SpicedCiderMixinCanceller implements MixinCanceller {

    @Override
    public boolean shouldCancel(List<String> targetClassNames, String mixinClassName) {
        return "mod.adrenix.nostalgic.mixin.tweak.candy.missing_texture.MissingTextureAtlasSpriteMixin".equals(mixinClassName);
    }
}