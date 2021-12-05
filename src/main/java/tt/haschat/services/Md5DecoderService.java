package tt.haschat.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tt.haschat.config.properties.Md5Properties;
import tt.haschat.dto.Response;
import tt.haschat.exceptions.DecodeException;
import tt.haschat.services.api.IMd5DecoderService;
import tt.haschat.services.api.IResponseService;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class Md5DecoderService implements IMd5DecoderService {

    private static final String HASH_PROPERTY_NAME = "hash";
    private static final String HASH_TYPE_PROPERTY_NAME = "hash_type";
    private static final String EMAIL_PROPERTY_NAME = "email";
    private static final String CODE_PROPERTY_NAME = "code";
    private static final String VENDOR_ERROR_MESSAGE =  "MD5 DECRYPT VENDOR SEND ERROR RESPONSE FOR HASH %s.\n%s.\nERROR DESCRIPTION: %s";

    private final String md5DecoderUrlTemplate;
    private final IResponseService responseService;
    private final RestTemplate restTemplate;
    private final Map<String, String> propertiesMap;
    private final MessageSource messageSource;

    public Md5DecoderService(
            IResponseService responseService,
            Md5Properties md5Properties,
            RestTemplate restTemplate,
            MessageSource messageSource
    ) {
        this.responseService = responseService;
        this.restTemplate = restTemplate;
        this.messageSource = messageSource;
        this.propertiesMap = this.createDefaultPropertiesMap(md5Properties);
        this.md5DecoderUrlTemplate = this.createUrlTemplate(md5Properties);
    }

    @Override
    public List<Response> decode(List<String> hashes) {
        List<Response> readHashes = this.responseService.getAllByHashList(hashes);
        if (!readHashes.isEmpty()) {
            Set<String> foundedHashSet = readHashes
                    .stream()
                    .map(Response::getHash)
                    .collect(Collectors.toSet());
            hashes.removeAll(foundedHashSet);
        }

        List<Response> decodeHashes = this.decodeAll(hashes.toArray(new String[0]));
        return Stream.concat(readHashes.stream(), decodeHashes.stream())
                .collect(Collectors.toList());

    }


    private List<Response> decodeAll(String[] hashes) {

        String hashesString = String.join(";", hashes);

        Map<String, String> variablesMap = new HashMap<>(this.propertiesMap);
        variablesMap.put(HASH_PROPERTY_NAME, hashesString);

        String resp;
        try {
            resp = restTemplate.getForObject(md5DecoderUrlTemplate, String.class, variablesMap);
        } catch (Exception e) {
            String msg = messageSource.getMessage("decode.error.default", null, LocaleContextHolder.getLocale());
            log.error(msg, e);
            throw new DecodeException(msg, e);
        }

        return this.createResponses(hashes, resp);
    }

    private List<Response> createResponses(String[] hashes, String resp) {
        if (hashes == null || hashes.length == 0) {
            return Collections.EMPTY_LIST;
        }

        if (resp == null) {
            resp = "";
            return Collections.singletonList(new Response(hashes[0], resp));
        }

        String[] responses = resp.replaceAll(";", " ;").split(";");
        List<Response> result = new ArrayList<>();

        for (int i = 0; i < hashes.length; i++) {
            String response = responses[i].trim();
            VendorErrorCodes err = VendorErrorCodes.getByCode(response);
            if (err != null) {
                log.warn(String.format(VENDOR_ERROR_MESSAGE, hashes[i], err.code, err.description));
                response = "";
            }
            result.add(new Response(hashes[i], response));
        }
        return result;
    }

    private String createUrlTemplate(Md5Properties md5Properties) {
        return UriComponentsBuilder.fromHttpUrl(md5Properties.getUrl())
                .queryParam(HASH_PROPERTY_NAME, "{" + HASH_PROPERTY_NAME + "}")
                .queryParam(HASH_TYPE_PROPERTY_NAME, "{" + HASH_TYPE_PROPERTY_NAME + "}")
                .queryParam(EMAIL_PROPERTY_NAME, "{" + EMAIL_PROPERTY_NAME + "}")
                .queryParam(CODE_PROPERTY_NAME, "{" + CODE_PROPERTY_NAME + "}")
                .encode()
                .toUriString();
    }

    private Map<String, String> createDefaultPropertiesMap(Md5Properties md5Properties) {
        return Map.of(
                HASH_TYPE_PROPERTY_NAME, md5Properties.getType(),
                EMAIL_PROPERTY_NAME, md5Properties.getEmail(),
                CODE_PROPERTY_NAME, md5Properties.getCode()
        );
    }

    private enum VendorErrorCodes {
        ERROR_CODE_001("ERROR CODE : 001" ,"You exceeded the 400 allowed request per day (please contact me if you need more than that)"),
        ERROR_CODE_002("ERROR CODE : 002", "There is an error in your email / code."),
        ERROR_CODE_003("ERROR CODE : 003", "Your request includes more than 400 hashes."),
        ERROR_CODE_004("ERROR CODE : 004", "The type of hash you provide in the argument hash_type doesn't seem to be valid."),
        ERROR_CODE_005("ERROR CODE : 005", "The hash you provide doesn't seem to match with the type of hash you set."),
        ERROR_CODE_006("ERROR CODE : 006", "You didn't provide all the arguments, or you mispell one of them.");

        private final String code;
        private final String description;

        VendorErrorCodes(String code, String description){
            this.code = code;
            this.description = description;
        }

        public static VendorErrorCodes getByCode(String code){
            return Arrays.stream(VendorErrorCodes.values())
                    .filter(e -> e.code.equals(code))
                    .findFirst()
                    .orElse(null);
        }
    }
}
