/**
 *
 */

package com.cmcc.hyapps.andyou.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cmcc.hyapps.andyou.R;

/**
 * 自定义 ActionBar
 *
 * @author kuloud
 */
public class ActionBar extends RelativeLayout {
    private ImageView mLeft;
    private TextView mLeftText;
    private ImageView mRight;
    private ImageView mRight2;
    private TextView mRightText;
    private TextView mTitle;
    private View divide;

    /**
     * 重写构造
     *
     * @param context
     */
    public ActionBar(Context context) {
        super(context);
    }

    public ActionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ActionBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mLeft = (ImageView) findViewById(R.id.action_bar_left);
        mLeftText = (TextView) findViewById(R.id.action_bar_left_text);
        mRight = (ImageView) findViewById(R.id.action_bar_right);
        mRight2 = (ImageView) findViewById(R.id.action_bar_right2);
        mRightText = (TextView) findViewById(R.id.action_bar_right_text);
        mTitle = (TextView) findViewById(R.id.action_bar_title);
        divide = (View) findViewById(R.id.action_bar_divide);
    }


    /**
     * 设置视图显示模式
     */
    public void setLeftMode(boolean isText) {
        if (isText) {
            mLeft.setVisibility(GONE);
            mLeftText.setVisibility(VISIBLE);
        } else {
            mLeft.setVisibility(VISIBLE);
            mLeftText.setVisibility(GONE);
        }
    }

    public void setRightMode(boolean isText) {
        if (isText) {
            mRight.setVisibility(GONE);
            mRightText.setVisibility(VISIBLE);
        } else {
            mRight.setVisibility(VISIBLE);
            mRightText.setVisibility(GONE);
        }
    }

    /**
     * 分别获取各个视图的view
     */


    public View getBarDivide() {
        return divide;
    }

    public ImageView getLeftView() {
        return mLeft;
    }

    public TextView getLeftTextView() {
        return mLeftText;
    }

    public TextView getTitleView() {
        return mTitle;
    }

    public ImageView getRightView() {
        return mRight;
    }

    public ImageView getRight2View() {
        return mRight2;
    }

    public TextView getRightTextView() {
        return mRightText;
    }

    /**
     *  设置标题
     */
    public void setTitle(int titleId) {
        mTitle.setText(titleId);
    }

    public void setTitle(CharSequence title) {
        mTitle.setText(title);
    }

}
