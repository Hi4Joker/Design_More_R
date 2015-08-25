package com.joker.app.view.circularProgress;

class StateManager {

  private volatile boolean saveIsEnabled;
  private volatile float saveProgress;

  public StateManager(StateInterface progressButton) {
    saveIsEnabled = progressButton.getEnabled();
    saveProgress = progressButton.getProgress();
  }

  public void saveProgress(StateInterface progressButton) {
    saveProgress = progressButton.getProgress();
  }

  public boolean getSaveIsEnabled() {
    return saveIsEnabled;
  }

  public float getSaveProgress() {
    return saveProgress;
  }

  public void checkState(StateInterface progressButton) {

    if (progressButton.getProgress() != saveProgress) {

      progressButton.setProgress(progressButton.getProgress());
    } else if (progressButton.getEnabled() != saveIsEnabled) {

      progressButton.setEnabled(progressButton.getEnabled());
    }
  }

  public interface StateInterface {

    boolean getEnabled();

    void setEnabled(boolean isEnable);

    float getProgress();

    void setProgress(float progress);
  }
}
