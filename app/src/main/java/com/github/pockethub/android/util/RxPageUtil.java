package com.github.pockethub.android.util;

import com.github.pockethub.android.core.PageIterator;
import com.meisolsson.githubsdk.model.Page;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class RxPageUtil {

    public static  <B> Observable<Page<B>> getAllPages(
            PageIterator.GitHubRequest<Response<Page<B>>> pagedSingleCall, int i) {

        return pagedSingleCall.execute(i)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMapObservable(response -> {
                    Page<B> page = response.body();
                    if (page.next() == null) {
                        return Observable.just(page);
                    }

                    return Observable.just(page)
                            .concatWith(getAllPages(pagedSingleCall, page.next()));

                });
    }
}
