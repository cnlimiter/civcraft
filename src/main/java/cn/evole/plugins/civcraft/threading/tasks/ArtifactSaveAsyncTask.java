package cn.evole.plugins.civcraft.threading.tasks;

import cn.evole.plugins.civcraft.structure.Ordinary;

public class ArtifactSaveAsyncTask
        implements Runnable {
    Ordinary ordinary;

    public ArtifactSaveAsyncTask(Ordinary ordinary) {
        this.ordinary = ordinary;
    }

    @Override
    public void run() {
        this.ordinary.saveProgress();
    }
}

