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

package com.afollestad.aesthetic;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function3;

import static com.afollestad.aesthetic.Rx.onErrorLogAndRethrow;

/**
 * @author Aidan Follestad (afollestad)
 */
public class AestheticBottomNavigationView extends BottomNavigationView {

    private Disposable modesSubscription;
    private CompositeDisposable colorSubscriptions;
    private int lastTextIconColor;

    public AestheticBottomNavigationView(Context context) {
        super(context);
    }

    public AestheticBottomNavigationView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AestheticBottomNavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void invalidateIconTextColor(int backgroundColor, int selectedColor) {
        int baseColor = ContextCompat.getColor(getContext(), Util.isColorLight(backgroundColor) ? R.color.ate_icon_light : R.color.ate_icon_dark);
        int unselectedIconTextColor = Util.adjustAlpha(baseColor, .87f);
        ColorStateList iconColor =
                new ColorStateList(
                        new int[][]{new int[]{-android.R.attr.state_checked}, new int[]{android.R.attr.state_checked}},
                        new int[]{unselectedIconTextColor, selectedColor});
        ColorStateList textColor = new ColorStateList(
                new int[][]{new int[]{-android.R.attr.state_checked}, new int[]{android.R.attr.state_checked}},
                new int[]{unselectedIconTextColor, selectedColor});
        setItemIconTintList(iconColor);
        setItemTextColor(textColor);
    }

    @Override
    public void setBackgroundColor(@ColorInt int color) {
        super.setBackgroundColor(color);
        if (lastTextIconColor == Color.TRANSPARENT) {
            lastTextIconColor = Util.isColorLight(color) ? Color.BLACK : Color.WHITE;
        }
        invalidateIconTextColor(color, lastTextIconColor);
    }

    private void onState(State state) {
        if (colorSubscriptions != null) {
            colorSubscriptions.clear();
        }
        colorSubscriptions = new CompositeDisposable();
        switch (state.iconTextMode) {
            case BottomNavIconTextMode.SELECTED_PRIMARY:
                colorSubscriptions.add(
                        Aesthetic.get()
                                .colorPrimary()
                                .compose(Rx.distinctToMainThread())
                                .subscribe(color -> lastTextIconColor = color, onErrorLogAndRethrow()));
                break;
            case BottomNavIconTextMode.SELECTED_ACCENT:
                colorSubscriptions.add(
                        Aesthetic.get()
                                .colorAccent()
                                .compose(Rx.distinctToMainThread())
                                .subscribe(color -> lastTextIconColor = color, onErrorLogAndRethrow()));
                break;
            case BottomNavIconTextMode.BLACK_WHITE_AUTO:
                // We will automatically set the icon/text color when the background color is set
                lastTextIconColor = Color.TRANSPARENT;
                break;
            default:
                throw new IllegalStateException("Unknown bottom nav icon/text mode: " + state.iconTextMode);
        }

        switch (state.bgMode) {
            case BottomNavBgMode.PRIMARY:
                colorSubscriptions.add(
                        Aesthetic.get()
                                .colorPrimary()
                                .compose(Rx.distinctToMainThread())
                                .subscribe(ViewBackgroundAction.create(this), onErrorLogAndRethrow()));
                break;
            case BottomNavBgMode.PRIMARY_DARK:
                colorSubscriptions.add(
                        Aesthetic.get()
                                .colorStatusBar()
                                .compose(Rx.distinctToMainThread())
                                .subscribe(ViewBackgroundAction.create(this), onErrorLogAndRethrow()));
                break;
            case BottomNavBgMode.ACCENT:
                colorSubscriptions.add(
                        Aesthetic.get()
                                .colorAccent()
                                .compose(Rx.distinctToMainThread())
                                .subscribe(ViewBackgroundAction.create(this), onErrorLogAndRethrow()));
                break;
            case BottomNavBgMode.BLACK_WHITE_AUTO:
                setBackgroundColor(ContextCompat.getColor(getContext(), state.isDark ? R.color.ate_bottom_nav_default_dark_bg : R.color.ate_bottom_nav_default_light_bg));
                break;
            default:
                throw new IllegalStateException("Unknown bottom nav bg mode: " + state.bgMode);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        modesSubscription = Observable.combineLatest(
                Aesthetic.get().bottomNavigationBackgroundMode(),
                Aesthetic.get().bottomNavigationIconTextMode(),
                Aesthetic.get().isDark(),
                State.creator())
                .compose(Rx.distinctToMainThread())
                .subscribe(this::onState, onErrorLogAndRethrow());
    }

    @Override
    protected void onDetachedFromWindow() {
        if (modesSubscription != null) {
            modesSubscription.dispose();
        }
        if (colorSubscriptions != null) {
            colorSubscriptions.clear();
        }
        super.onDetachedFromWindow();
    }

    private static class State {

        @BottomNavBgMode
        private final int bgMode;
        @BottomNavIconTextMode
        private final int iconTextMode;
        private final boolean isDark;

        private State(int bgMode, int iconTextMode, boolean isDark) {
            this.bgMode = bgMode;
            this.iconTextMode = iconTextMode;
            this.isDark = isDark;
        }

        static State create(@BottomNavBgMode int bgMode, @BottomNavIconTextMode int iconTextMode, boolean isDark) {
            return new State(bgMode, iconTextMode, isDark);
        }

        static Function3<Integer, Integer, Boolean, State> creator() {
            return State::create;
        }
    }
}
