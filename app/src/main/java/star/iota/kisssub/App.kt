/*
 *
 *  *    Copyright 2018. iota9star
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package star.iota.kisssub

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.support.multidex.MultiDex
import com.afollestad.aesthetic.AestheticStoreHouseHeader
import com.crashlytics.android.Crashlytics
import com.lzy.okgo.OkGo
import com.lzy.okgo.cache.CacheEntity
import com.lzy.okgo.cache.CacheMode
import com.lzy.okgo.cookie.CookieJarImpl
import com.lzy.okgo.cookie.store.DBCookieStore
import com.lzy.okgo.https.HttpsUtils
import com.lzy.okgo.model.HttpHeaders
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.footer.ClassicsFooter
import com.zzhoujay.richtext.RichText
import io.fabric.sdk.android.Fabric
import okhttp3.OkHttpClient
import star.iota.kisssub.helper.ThemeHelper
import java.util.concurrent.TimeUnit


class App : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    companion object {
        fun makeOkHttpClient(context: Context): OkHttpClient {
            val builder = OkHttpClient.Builder()
//            val loggingInterceptor = HttpLoggingInterceptor("okhttp")
//            loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY)
//            loggingInterceptor.setColorLevel(Level.INFO)
//            builder.addNetworkInterceptor(StethoInterceptor())
            builder.readTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS)
            builder.writeTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS)
            builder.connectTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS)
            builder.cookieJar(CookieJarImpl(DBCookieStore(context)))
            val sslParams = HttpsUtils.getSslSocketFactory()
            builder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
            return builder.build()
        }

        init {
            SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, _ ->
                AestheticStoreHouseHeader(context)
                        .initWithString(context.resources.getString(R.string.kisssub))
                        .setLineWidth(8)
                        .setLoadingAniDuration(800)
                        .setDropHeight(context.resources.getDimensionPixelSize(R.dimen.v256dp))
                        .setHeaderBackgroundColor(Color.TRANSPARENT)
                        .setTextColor(ThemeHelper.getAccentColor(context))
            }
            SmartRefreshLayout.setDefaultRefreshFooterCreator { context, _ -> ClassicsFooter(context) }
        }
    }

    override fun onCreate() {
        super.onCreate()
//        LeakCanary.install(this)
        Fabric.with(this, Crashlytics())
        RichText.initCacheDir(this)
//        Stetho.initializeWithDefaults(this)
        initOkGo()
    }

    override fun onTerminate() {
        super.onTerminate()
        RichText.recycle()
    }


    private fun initOkGo() {
        val version = this.packageManager.getPackageInfo(this.packageName, PackageManager.GET_CONFIGURATIONS).versionCode
        val ua = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.32 Safari/537.36 Kisssub/$version"
        val headers = HttpHeaders()
        headers.put("User-Agent", ua)
        OkGo.getInstance()
                .init(this)
                .setOkHttpClient(makeOkHttpClient(this))
                .setCacheMode(CacheMode.REQUEST_FAILED_READ_CACHE)
                .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)
                .setRetryCount(3)
                .addCommonHeaders(headers)
    }
}
