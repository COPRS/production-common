curl -XPUT "http://localhost:9200/prip" -H 'Content-Type: application/json' -d '{"mappings":{"properties":{"id":{"type":"text"},"obsKey":{"type":"text"},"name":{"type":"text"},"productFamily":{"type":"text"},"contentType":{"type":"text"},"contentLength":{"type":"long"},"creationDate":{"type":"date"},"evictionDate":{"type":"date"},"checksum":{"type":"nested","properties":{"algorithm":{"type":"text"},"value":{"type":"text"},"checksum_date":{"type":"date"}}},"footprint":{"type":"geo_shape","tree":"geohash"}}}}'