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

package star.iota.kisssub.ui.about

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.SystemClock
import android.view.View
import com.lzy.okgo.OkGo
import kotlinx.android.synthetic.main.activity_about.*
import okhttp3.HttpUrl
import star.iota.kisssub.KisssubUrl
import star.iota.kisssub.R
import star.iota.kisssub.base.BaseActivity
import star.iota.kisssub.glide.GlideApp
import star.iota.kisssub.helper.ThemeHelper
import star.iota.kisssub.utils.SendUtils
import star.iota.kisssub.utils.UpdateUtils
import star.iota.kisssub.widget.MessageBar

class AboutActivity : BaseActivity(), View.OnClickListener, InfoContract.View, GodModeContract.View {
    override fun other(e: String?) {
        MessageBar.create(this, e)
        endGodMode()
    }

    override fun isActivated(activate: Boolean) {
        if (activate) {
            MessageBar.create(this, "成功激活上帝模式")
            linearLayoutGodModeContainer.visibility = View.GONE
            textViewAppName.text = (getString(R.string.kisssub) + " - GOD")
        } else {
            textInputEditTextCode.error = "神秘代码不正确"
            textInputEditTextCode.requestFocus()
        }
        endGodMode()
    }

    private var godModeIsLoading = false
    private fun endGodMode() {
        godModeIsLoading = false
        progressBarGodMode?.visibility = View.GONE
    }

    override fun success(info: InfoBean) {
        endUpdate()
        UpdateUtils.show(this, info, true)
    }

    override fun error(e: String?) {
        endUpdate()
    }

    override fun noData() {
        endUpdate()
        MessageBar.create(this, "没有获得版本信息")
    }

    private fun endUpdate() {
        updateIsLoading = false
        progressBarUpdate?.visibility = View.GONE
    }

    override fun getContentViewId(): Int = R.layout.activity_about

    private val mHints = LongArray(5)
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.textViewSource -> {
                SendUtils.open(this, getString(R.string.about_kisssub_url))
            }
            R.id.linearLayoutGrade -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + this.packageName)))
            }
            R.id.linearLayoutCheckUpdate -> {
                if (!updateIsLoading) {
                    updateIsLoading = true
                    progressBarUpdate?.visibility = View.VISIBLE
                    updatePresenter.get(KisssubUrl.UPDATE_URL)
                } else {
                    MessageBar.create(this, "正在加载版本信息中，请等待")
                }
            }
            R.id.linearLayoutGodMode -> {
                System.arraycopy(mHints, 1, mHints, 0, mHints.size - 1)
                mHints[mHints.size - 1] = SystemClock.uptimeMillis()
                if (SystemClock.uptimeMillis() - mHints[0] <= 2400) {
                    linearLayoutGodModeContainer.visibility = View.VISIBLE
                }
            }
            R.id.buttonGodMode -> {
                val code = textInputEditTextCode.text.toString().trim()
                if (code.isBlank()) {
                    MessageBar.create(this, "请输入神秘代码")
                } else {
                    if (!godModeIsLoading) {
                        godModeIsLoading = true
                        progressBarGodMode?.visibility = View.VISIBLE
                        godModePresenter.get(KisssubUrl.GOD_MODE, code)
                    } else {
                        MessageBar.create(this, "正在加载中，请等待")
                    }
                }
            }
        }
    }


    private fun initEvent() {
        textViewSource?.setOnClickListener(this)
        linearLayoutGrade?.setOnClickListener(this)
        linearLayoutCheckUpdate?.setOnClickListener(this)
        linearLayoutGodMode?.setOnClickListener(this)
        buttonGodMode?.setOnClickListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        updatePresenter.unsubscribe()
    }

    private var updateIsLoading = false
    private lateinit var updatePresenter: InfoPresenter
    private lateinit var godModePresenter: GodModePresenter
    override fun doSome() {
        isGodMode()
        initPresenter()
        initEvent()
        GlideApp.with(this)
                .load(ThemeHelper.getDynamicBanner(this))
                .into(kenBurnsView)
        try {
            val packageInfo = this.packageManager.getPackageInfo(this.packageName, PackageManager.GET_CONFIGURATIONS)
            textViewVersion?.text = ("${packageInfo.versionName}  『 ${packageInfo.versionCode} 』")
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun isGodMode() {
        val cookieStore = OkGo.getInstance().cookieJar.cookieStore
        val httpUrl = HttpUrl.parse(KisssubUrl.GOD_MODE)
        val cookies = cookieStore.getCookie(httpUrl)
        val pattern = "god_mode\\s*="
        if (cookies.toString().contains(Regex(pattern))) {
            textViewAppName.text = (getString(R.string.kisssub) + " - GOD")
        }
    }

    private fun initPresenter() {
        updatePresenter = InfoPresenter(this)
        godModePresenter = GodModePresenter(this)
    }

    companion object {
        fun newInstance() = AboutActivity()
    }
}
