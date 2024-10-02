package org.pac4j.oauth.profile.paypal;

import com.github.scribejava.core.model.Token;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.profile.converter.Converters;
import org.pac4j.oauth.config.OAuthConfiguration;
import org.pac4j.oauth.profile.JsonHelper;
import org.pac4j.oauth.profile.converter.JsonConverter;
import org.pac4j.oauth.profile.definition.OAuthProfileDefinition;

import java.util.Arrays;

import static org.pac4j.core.profile.AttributeLocation.PROFILE_ATTRIBUTE;

/**
 * This class is the PayPal profile definition.
 *
 * @author Jerome Leleu
 * @since 1.4.2
 */
public class PayPalProfileDefinition extends OAuthProfileDefinition {

    /** Constant <code>ADDRESS="address"</code> */
    public static final String ADDRESS = "address";
    /** Constant <code>LANGUAGE="language"</code> */
    public static final String LANGUAGE = "language";
    /** Constant <code>ZONEINFO="zoneinfo"</code> */
    public static final String ZONEINFO = "zoneinfo";
    /** Constant <code>NAME="name"</code> */
    public static final String NAME = "name";
    /** Constant <code>GIVEN_NAME="given_name"</code> */
    public static final String GIVEN_NAME = "given_name";

    /**
     * <p>Constructor for PayPalProfileDefinition.</p>
     */
    public PayPalProfileDefinition() {
        super(x -> new PayPalProfile());
        Arrays.stream(new String[] {ZONEINFO, NAME, GIVEN_NAME}).forEach(a -> primary(a, Converters.STRING));
        primary(ADDRESS, new JsonConverter(PayPalAddress.class));
        primary(LANGUAGE, Converters.LOCALE);
    }

    /** {@inheritDoc} */
    @Override
    public String getProfileUrl(final Token accessToken, final OAuthConfiguration configuration) {
        return "https://api.paypal.com/v1/identity/openidconnect/userinfo?schema=openid";
    }

    /** {@inheritDoc} */
    @Override
    public PayPalProfile extractUserProfile(final String body) {
        val profile = (PayPalProfile) newProfile();
        val json = JsonHelper.getFirstNode(body);
        if (json != null) {
            val userId = (String) JsonHelper.getElement(json, "user_id");
            profile.setId(StringUtils.substringAfter(userId, "/user/"));
            for (val attribute : getPrimaryAttributes()) {
                convertAndAdd(profile, PROFILE_ATTRIBUTE, attribute, JsonHelper.getElement(json, attribute));
            }
        } else {
            raiseProfileExtractionJsonError(body);
        }
        return profile;
    }
}
