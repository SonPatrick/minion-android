package com.tomclaw.minion.demo.utils;

/**
 * Created with IntelliJ IDEA.
 * User: Solkin
 * Date: 31.10.13
 * Time: 11:08
 */
@SuppressWarnings("WeakerAccess")
public abstract class Task implements Runnable {

    @Override
    public void run() {
        try {
            executeBackground();
            onSuccessBackground();
            MainExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    onPostExecuteMain();
                    onSuccessMain();
                }
            });
        } catch (final Throwable ex) {
            onFailBackground();
            MainExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    onPostExecuteMain();
                    onFailMain(ex);
                }
            });
        }
    }

    public boolean isPreExecuteRequired() {
        return false;
    }

    public void onPreExecuteMain() {
    }

    public abstract void executeBackground() throws Throwable;

    public void onPostExecuteMain() {
    }

    public void onSuccessBackground() {
    }

    public void onFailBackground() {
    }

    public void onSuccessMain() {
    }

    public void onFailMain(Throwable ex) {
    }
}
