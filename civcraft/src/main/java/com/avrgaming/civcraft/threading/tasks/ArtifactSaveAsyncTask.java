
package com.avrgaming.civcraft.threading.tasks;

import com.avrgaming.civcraft.structure.Ordinary;

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

