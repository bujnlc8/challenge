package site.haihui.challenge.utils;

import java.io.IOException;
import java.util.TreeMap;

import com.tencent.cloud.CosStsClient;
import com.tencent.cloud.Response;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class QcloudUtil {

    @Value("${qcloud.secrect_id}")
    private String secrectId;

    @Value("${qcloud.secrect_key}")
    private String secrectKey;

    @Value("${qcloud.bucket}")
    private String bucket;

    @Value("${qcloud.region}")
    private String region;

    @Value("#{'${qcloud.allow_prefix}'.split(',')}")
    private String[] allowPrefix;

    @Value("#{'${qcloud.allow_action}'.split(',')}")
    private String[] allowActions;

    private TreeMap<String, Object> getConfig() {
        TreeMap<String, Object> config = new TreeMap<String, Object>();
        config.put("secretId", secrectId);
        config.put("secretKey", secrectKey);
        config.put("durationSeconds", 7200);
        config.put("bucket", bucket);
        config.put("region", region);
        config.put("allowPrefixes", allowPrefix);
        config.put("allowActions", allowActions);
        return config;
    }

    public Response getTemporaryKey() {
        try {
            return CosStsClient.getCredential(getConfig());
        } catch (IOException e) {
            log.error("getTemporaryKey error occur", e);
        }
        return null;
    }
}
