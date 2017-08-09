package com.tomclaw.minion.demo.benchmark;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.tomclaw.minion.Minion;
import com.tomclaw.minion.demo.utils.MainExecutor;
import com.tomclaw.minion.demo.utils.Task;

import java.util.concurrent.TimeUnit;

/**
 * Created by solkin on 09.08.17.
 */
public abstract class BenchmarkTask extends Task {

    private static final long DEBOUNCE_DELAY = 500;
    private
    @NonNull
    BenchmarkItem benchmarkItem;
    private int position;
    private Minion minion;
    private
    @NonNull
    BenchmarkRecyclerAdapter adapter;
    private
    @NonNull
    BenchmarkCallback callback;
    private long lastUpdateTime;
    private
    AdapterUpdater adapterUpdater;

    public BenchmarkTask(@NonNull Minion minion, @NonNull BenchmarkRecyclerAdapter adapter, @NonNull BenchmarkCallback callback) {
        this.minion = minion;
        this.adapter = adapter;
        this.callback = callback;
        this.benchmarkItem = createItem();
    }

    @Override
    public final boolean isPreExecuteRequired() {
        return true;
    }

    @Override
    public final void onPreExecuteMain() {
        position = adapter.appendItem(benchmarkItem);
        adapterUpdater = new AdapterUpdater(adapter, position);
        adapter.notifyItemInserted(position);
    }

    @Override
    public final void onPostExecuteMain() {
        setProgress(100);
        updateItemInternal();
        callback.onComplete(benchmarkItem.getId());
    }

    private BenchmarkItem createItem() {
        return new BenchmarkItem(getId(), getTitle(), 0, "");
    }

    protected abstract int getId();

    protected abstract
    @StringRes
    int getTitle();

    protected abstract int getTestsCount();

    @Override
    public final void executeBackground() throws Throwable {
        Minion minion = getMinion();
        beforeTest(minion);
        int count = getTestsCount();
        long time = System.currentTimeMillis();
        for (int c = 0; c < count; c++) {
            runTest(minion);
            setProgress(100 * c / count);
            updateItem();
        }
        time -= System.currentTimeMillis();
        time *= -1;
        setResult((count * TimeUnit.SECONDS.toMillis(1) / time) + " ops/sec");
    }

    protected abstract void beforeTest(Minion minion);

    protected abstract void runTest(Minion minion);

    public void setProgress(int progress) {
        benchmarkItem.setProgress(progress);
    }

    public void setResult(@NonNull String result) {
        benchmarkItem.setResult(result);
    }

    void updateItem() {
        if (System.currentTimeMillis() - lastUpdateTime > DEBOUNCE_DELAY) {
            updateItemInternal();
            lastUpdateTime = System.currentTimeMillis();
        }
    }

    private void updateItemInternal() {
        MainExecutor.execute(adapterUpdater);
    }

    public Minion getMinion() {
        return minion;
    }

    private static class AdapterUpdater implements Runnable {

        private
        @NonNull
        BenchmarkRecyclerAdapter adapter;
        private int position;

        private AdapterUpdater(@NonNull BenchmarkRecyclerAdapter adapter, int position) {
            this.adapter = adapter;
            this.position = position;
        }

        @Override
        public void run() {
            adapter.notifyItemChanged(position);
        }
    }

    public interface BenchmarkCallback {

        void onComplete(int id);

    }
}
