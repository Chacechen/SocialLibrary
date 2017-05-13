package com.vendor.social.share;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.vendor.social.R;
import com.vendor.social.ShareApi;
import com.vendor.social.SocialConfig;
import com.vendor.social.model.ShareType;
import com.vendor.social.utils.BitmapLoader;

import java.io.ByteArrayOutputStream;

/**
 * 分享平台公共组件模块-微信分享
 * Created by ljfan on 16/4/19.
 */
public class WeixinShare extends ShareApi{

    private static final int THUMB_SIZE = 100;

    public WeixinShare(Activity act) {
        super(act);
        setShareType(ShareType.WEIXIN);
    }

    @Override
    public void doShare(){
        //获取bitmap
        new BitmapLoader().loadIconBitmap(mActivity, getShareContent(), new BitmapLoader.OnLoadImageListener() {
            @Override
            public void onResult(Bitmap bitmap) {
                if(bitmap != null){
                    IWXAPI api = WXAPIFactory.createWXAPI(mActivity, SocialConfig.getWeixinId(), true);
                    api.registerApp(SocialConfig.getWeixinId());

                    if(!api.isWXAppInstalled()) {
                        Toast.makeText(mActivity, R.string.social_fail_weixin_un_install, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    WXWebpageObject webpage = new WXWebpageObject();
                    webpage.webpageUrl = getShareContent().getTargetUrl();
                    WXMediaMessage msg = new WXMediaMessage(webpage);
                    msg.title = getShareContent().getTitle();//不能太长，否则微信会提示出错。不过没验证过具体能输入多长。
                    msg.description = getShareContent().getText();
                    Bitmap thumb = Bitmap.createScaledBitmap(bitmap, THUMB_SIZE, THUMB_SIZE, true);
                    msg.thumbData = Util.bmpToByteArray(thumb, true);

                    SendMessageToWX.Req req = new SendMessageToWX.Req();
                    req.transaction = buildTransaction("webpage");
                    req.message = msg;
                    req.scene = SendMessageToWX.Req.WXSceneSession;
//                    req.openId = SocialConfig.getWeixinId();
                    api.sendReq(req);
                }else{
                    callbackShareFail("ImageLoader load image fail");
                }
            }
        });
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }
    
    private static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return result;
    }
}