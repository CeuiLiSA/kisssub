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

package star.iota.kisssub.room

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "record")
class Record {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    var type: Int = 0
    var category: String? = null
    var title: String? = null
    var date: String? = null
    var cover: String? = null
    var sub: String? = null
    var size: String? = null
    var url: String? = null
    var desc: String? = null
    var magnet: String? = null

    companion object {
        const val NO_IMAGE = 0
        const val WITH_IMAGE = 1
    }
}
