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

package star.iota.kisssub.ui.history

import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.item_history.view.*
import star.iota.kisssub.R
import star.iota.kisssub.base.BaseViewHolder
import star.iota.kisssub.ext.addFragmentToActivity
import star.iota.kisssub.helper.ThemeHelper
import star.iota.kisssub.ui.play.PlayFragment

class HistoryViewHolder(itemView: View) : BaseViewHolder<HistoryBean>(itemView) {

    override fun bindView(bean: HistoryBean) {
        itemView?.apply {
            val title = bean.title
            if (bean.title.isNullOrBlank()) {
                textViewDate?.text = "数据错误"
                textViewSeason?.text = "o(*≧▽≦)ツ"
            } else {
                textViewDate?.text = title!!.substring(0, title.length - 3)
                textViewSeason?.text = title.substring(title.length - 3, title.length)
            }
            linearLayoutContainer?.setOnClickListener {
                if (!bean.title.isNullOrBlank() && !bean.url.isNullOrBlank()) {
                    (context as AppCompatActivity).addFragmentToActivity(PlayFragment.newInstance(bean.title!!, bean.url!!), R.id.frameLayoutContainer)
                }
            }
            imageViewLabel?.setColorFilter(ThemeHelper.getAccentColor(context))
        }
    }

}
