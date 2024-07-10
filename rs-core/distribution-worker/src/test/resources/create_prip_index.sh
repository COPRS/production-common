# Copyright 2023 Airbus
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

curl -XPUT "http://localhost:9200/prip" -H 'Content-Type: application/json' -d '{"mappings":{"properties":{"id":{"type":"keyword"},"obsKey":{"type":"keyword"},"name":{"type":"keyword"},"productFamily":{"type":"keyword"},"contentType":{"type":"keyword"},"contentLength":{"type":"long"},"contentDateStart":{"type":"date"},"contentDateEnd":{"type":"date"},"creationDate":{"type":"date"},"evictionDate":{"type":"date"},"checksum":{"type":"nested","properties":{"algorithm":{"type":"keyword"},"value":{"type":"keyword"},"checksum_date":{"type":"date"}}},"footprint":{"type":"geo_shape"},"browseKeys":{"type":"keyword"},"online":{"type":"boolean"}}},"settings":{"analysis":{"analyzer":{"default":{"type":"keyword"}}}}}'
