package com.weibo.cjfire.downloadpractise.db;

import com.weibo.cjfire.downloadpractise.entities.ThreadInfo;

import java.util.List;

/**
 * Created by cjfire on 16/10/22.
 */

public interface ThreadDAO {

    public void insertThread(ThreadInfo threadInfo);
    public void deleteThread(ThreadInfo threadInfo);
    public void updateThreat(ThreadInfo threadInfo);
    public List<ThreadInfo>getThread(String url);
    public boolean isExists(ThreadInfo threadInfo);
}
