package vn.phatbee.cosmesticshopapp.cloudinary;

import android.content.Context;

import com.cloudinary.Cloudinary;
import com.cloudinary.android.MediaManager;

import vn.phatbee.cosmesticshopapp.R;

public class CloudinaryConfig {
    private static Cloudinary cloudinary;

    public static void init(Context context) {
        if (cloudinary == null) {
            String cloudName = context.getString(R.string.cloudinary_cloud_name);
            String apiKey = context.getString(R.string.cloudinary_api_key);
            String apiSecret = context.getString(R.string.cloudinary_api_secret);

            cloudinary = new Cloudinary();
            cloudinary.config.cloudName = cloudName;
            cloudinary.config.apiKey = apiKey;
            cloudinary.config.apiSecret = apiSecret;

            MediaManager.init(context, cloudinary.config);
        }
    }

    public static Cloudinary getCloudinary() {
        return cloudinary;
    }
}