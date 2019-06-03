package com.cgfay.filterlibrary.mp4compose.filter;

import android.opengl.GLES20;
import android.util.Log;
import android.util.Pair;


import com.cgfay.filterlibrary.mp4compose.gl.GlFramebufferObject;

import java.util.ArrayList;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FRAMEBUFFER;

public class GlFilterGroup extends GlFilter {

    private final ArrayList<GlFilter> filters;

    private final ArrayList<Pair<GlFilter, GlFramebufferObject>> list = new ArrayList<Pair<GlFilter, GlFramebufferObject>>();

    public GlFilterGroup(final GlFilter... glFilters) {
        filters = new ArrayList<>();
    }

    public GlFilter getFilterByIndex(int index){
        if(filters==null ) return null;
        if(index<0|| index>=filters.size()) return null;
        return filters.get(index);
    }

    public void addFilterItem(GlFilter filter){
        if(filter!=null && filters!=null){
            filters.add(filter);
        }
    }

    public GlFilterGroup(final ArrayList<GlFilter> glFilters) {
        filters = glFilters;
    }

    @Override
    public void setup() {
        super.setup();

        if (filters != null) {
            final int max = filters.size();
            int count = 0;

            for (final GlFilter shader : filters) {
                shader.setup();
                final GlFramebufferObject fbo;
                if ((count + 1) < max) {
                    fbo = new GlFramebufferObject();
                } else {
                    fbo = null;
                }
                list.add(Pair.create(shader, fbo));
                count++;
            }
        }
    }

    @Override
    public void release() {
        Log.e("Harrison","GlFramebufferObject***");
        for (final Pair<GlFilter, GlFramebufferObject> pair : list) {
            if (pair.first != null) {
                pair.first.release();
            }
            if (pair.second != null) {
                pair.second.release();
            }
        }
        list.clear();
       super.release();
    }

    @Override
    public void setFrameSize(final int width, final int height) {
        super.setFrameSize(width, height);

        for (final Pair<GlFilter, GlFramebufferObject> pair : list) {
            if (pair.first != null) {
                pair.first.setFrameSize(width, height);
            }
            if (pair.second != null) {
                pair.second.setup(width, height);
            }
        }
    }

    private int prevTexName;

    @Override
    public void draw(final int texName, final GlFramebufferObject fbo) {
        prevTexName = texName;
        for (final Pair<GlFilter, GlFramebufferObject> pair : list) {
            if (pair.second != null) {
                if (pair.first != null) {
                    pair.second.enable();
                    GLES20.glClear(GL_COLOR_BUFFER_BIT);

                    pair.first.draw(prevTexName, pair.second);
                }
                prevTexName = pair.second.getTexName();

            } else {
                if (fbo != null) {
                    fbo.enable();
                } else {
                    GLES20.glBindFramebuffer(GL_FRAMEBUFFER, 0);
                }

                if (pair.first != null) {
                    pair.first.draw(prevTexName, fbo);
                }
            }
        }
    }

}

