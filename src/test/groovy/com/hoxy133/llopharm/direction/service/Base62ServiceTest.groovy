package com.hoxy133.llopharm.direction.service

import spock.lang.Specification

class Base62ServiceTest extends Specification {

    private Base62Service base62Service;

    def setup(){
        base62Service = new Base62Service();
    }

    def "check base62 encoder/decoder"() {
        given:
        long num = 5;

        when:
        def encodeId = base62Service.encodeDirectionId(num);
        def decodeId = base62Service.decodeDirectionId(encodeId);

        then:
        num == decodeId;
    }

}
