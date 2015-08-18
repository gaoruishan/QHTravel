/**
 * 
 */

package com.cmcc.hyapps.andyou.adapter.row;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.cmcc.hyapps.andyou.R;
import com.cmcc.hyapps.andyou.adapter.BannerPagerAdapter.IActionCallback;
import com.cmcc.hyapps.andyou.model.HomeBanner;
import com.cmcc.hyapps.andyou.support.OnClickListener;
import com.cmcc.hyapps.andyou.util.ImageUtil;

/**
 * @author niuzhiguo
 */
public class RowBannerMarket {
    public static View getView(Context context, final HomeBanner banner, View convertView,
            ViewGroup container,
            final IActionCallback<HomeBanner> callback) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.layout_banner_market,
                    container, false);
            holder = new ViewHolder();
            holder.image = (NetworkImageView) convertView.findViewById(R.id.discovery_banner_image);
            holder.title = (TextView) convertView.findViewById(R.id.discovery_banner_title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (banner == null) {
            holder.image.setImageResource(R.mipmap.bg_banner_hint);
        } else {
            holder.image.setDefaultImageResId(R.mipmap.bg_banner_hint).setErrorImageResId(R.mipmap.bg_banner_hint);
            //holder.image.setImageUrl(banner.imageUrl, RequestManager.getInstance().getImageLoader());
            //holder.title.setText(banner.title);
            ImageUtil.DisplayImage(banner.image_url, holder.image);
//            holder.image.setImageUrl(banner.image_url, RequestManager.getInstance().getImageLoader());
            holder.image.setOnClickListener(new OnClickListener() {
                @Override
                public void onValidClick(View v) {
                    if (callback != null) {
                        callback.doAction(banner);
                    }
                }
            });
        }
        return convertView;
    }

    private static class ViewHolder {
        NetworkImageView image;
        TextView title;
    }
}
