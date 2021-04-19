package com.frx.libcommon.extention;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.elvishew.xlog.XLog;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 事件总线，支持粘性和非粘性
 */
public class LiveDataBus {
    private static class Lazy {
        static LiveDataBus sLiveDataBus = new LiveDataBus();
    }

    public static LiveDataBus get() {
        return Lazy.sLiveDataBus;
    }

    private final ConcurrentHashMap<String, StickyLiveData> hashMap = new ConcurrentHashMap();

    /**
     * 获取eventName对应的StickyLiveData
     *
     * @param eventName String
     * @return StickyLiveData
     */
    public StickyLiveData with(String eventName) {
        StickyLiveData liveData = hashMap.get(eventName);
        if (liveData == null) {
            liveData = new StickyLiveData(eventName);
            hashMap.put(eventName, liveData);
        }
        return liveData;
    }

    /**
     * 粘性LiveData
     *
     * @param <T>
     */
    public class StickyLiveData<T> extends LiveData<T> {

        private final String eventName;
        private T stickyData;

        //用于标记当前LiveData发送了多少次事件
        private int version = 0;

        public StickyLiveData(String eventName) {
            this.eventName = eventName;
        }

        @Override
        public void setValue(T value) {
            version++;
            super.setValue(value);
        }

        @Override
        public void postValue(T value) {
            version++;
            super.postValue(value);
        }

        public void setStickyData(T stickyData) {
            this.stickyData = stickyData;
            setValue(stickyData);
        }

        public void postStickyData(T stickyData) {
            this.stickyData = stickyData;
            postValue(stickyData);
        }

        /**
         * 发送非粘性事件
         *
         * @param owner    LifecycleOwner
         * @param observer Observer
         */
        @Override
        public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
            observerSticky(owner, observer, false);
        }

        /**
         * 发送事件
         *
         * @param owner    LifecycleOwner
         * @param observer Observer
         * @param sticky   boolean 粘性
         */
        public void observerSticky(LifecycleOwner owner, Observer<? super T> observer, boolean sticky) {
            super.observe(owner, new WrapperObserver(this, observer, sticky));
            owner.getLifecycle().addObserver(new LifecycleEventObserver() {
                @Override
                public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                    if (event == Lifecycle.Event.ON_DESTROY) {
                        hashMap.remove(eventName);
                    }
                }
            });
        }

        private class WrapperObserver<T> implements Observer<T> {

            private final StickyLiveData<T> liveData;
            private final Observer<T> observer;
            private final boolean sticky;

            //标记该liveData已经发射几次数据了，用以过滤老数据重复接收
            private int lastVersion = 0;

            public WrapperObserver(StickyLiveData<T> tStickyLiveData, Observer<T> observer, boolean sticky) {
                this.liveData = tStickyLiveData;
                this.observer = observer;
                this.sticky = sticky;

                //比如先使用StickyLiveData发送了一条数据。StickyLiveData#version=1
                //那当我们创建WrapperObserver注册进去的时候，就至少需要把它的version和 StickyLiveData的version保持一致
                //用以过滤老数据，否则 岂不是会收到老的数据？
                lastVersion = this.liveData.version;
            }

            @Override
            public void onChanged(T t) {
                //如果当前observer收到数据的次数已经大于等于了StickyLiveData发送数据的个数了则return
                //
                //observer.lastVersion >= liveData.version
                //这种情况 只会出现在，我们先行创建一个liveData发射了一条数据。此时liveData的version=1.
                //
                //而后注册一个observer进去。由于我们代理了传递进来的observer,进而包装成wrapperObserver，此
                //时wrapperObserver的lastVersion 就会跟liveData的version 对齐。保持一样。把wrapperObserver注册到liveData中。
                //
                //根据liveData的原理，一旦一个新的observer 注册进去,也是会尝试把数据派发给他的。这就是黏性事件(先发送,后接收)。
                //
                //但此时wrapperObserver的lastVersion 已经和 liveData的version 一样了。由此来控制黏性事件的分发与否
                //
                if (lastVersion >= liveData.version) {
                    //但如果当前observer它是关心黏性事件的，则给他。
                    if (sticky && liveData.stickyData != null) {
                        observer.onChanged(liveData.stickyData);
                    }
                    return;
                }
                lastVersion = liveData.version;
                observer.onChanged(t);
            }
        }
    }
}
