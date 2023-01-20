# RS Core - Elasticsearch Configuration

The application used in the Copernicus Refrence System is relying on an ElasticSearch instance to save and provide metadata extracted from all incoming products. 
Before starting the first RS Core stream it is therefore mandatory to initialize all necessary indices.

## List of indices with mappings

As different indices need to be initialized with different mappings, the list is split into different groups that share the same mappings:

```
mpl_orbpre
mpl_orbres
mpl_orbsct
aux_obmemc
aux_poeorb
aux_preorb
aux_resorb
aux_cal
aux_pp1
aux_pp2
aux_ins
aux_wnd
aux_ice
aux_wav
raw
session
aux_ece
aux_scs
aux_itc
aux_scf
aux_tec
aux_tro
s2_aux
s3_aux
```

```
{"mappings":{"properties":{"creationTime":{"type":"date"},"insertionTime":{"type":"date"}}}}
```

-------------------

```
l0_segment
```

```
{"mappings":{"properties":{"insertionTime":{"type":"date"},"segmentCoordinates":{"type":"geo_shape"}}}}
```

-------------------

```
aux_att
```

```
{"mappings":{"properties":{"creationTime":{"type":"date"},"insertionTime":{"type":"date"},"instrumentConfigurationId":{"type":"long"},"missionId":{"type":"keyword"},"productFamily":{"type":"keyword"},"productName":{"type":"keyword"},"productType":{"type":"keyword"},"satelliteId":{"type":"keyword"},"site":{"type":"keyword"},"url":{"type":"keyword"},"validityStartTime":{"type":"date"},"validityStopTime":{"type":"date"}}}}
```

-------------------

```
l0_slice
l0_acn
l1_slice
l1_acn
l2_slice
l2_acn
s2_l0_ds
s2_l0_gr
s2_hktm
s2_sad
s3_granules
s3_l0
s3_cal
s3_pug
```

```
{"mappings":{"properties":{"creationTime":{"type":"date"},"startTime":{"type":"date"},"sliceCoordinates":{"type":"geo_shape"},"oqcFlag":{"type":"text"},"insertionTime":{"type":"date"}}}}
```

-------------------

```
prip
```

```
{"mappings":{"properties":{"id":{"type":"keyword"},"obsKey":{"type":"keyword"},"name":{"type":"keyword"},"productFamily":{"type":"keyword"},"contentType":{"type":"keyword"},"contentLength":{"type":"long"},"contentDateStart":{"type":"date"},"contentDateEnd":{"type":"date"},"creationDate":{"type":"date"},"evictionDate":{"type":"date"},"checksum":{"type":"nested","properties":{"algorithm":{"type":"keyword"},"value":{"type":"keyword"},"checksum_date":{"type":"date"}}},"footprint":{"type":"geo_shape"},"browseKeys":{"type":"keyword"},"online":{"type":"boolean"}}},"settings":{"analysis":{"analyzer":{"default":{"type":"keyword"}}}}}
```

-------------------

```
data-lifecycle-metadata
```

```
{"mappings":{"properties":{"ProductName":{"type":"text","analyzer":"keyword","fields":{"keyword":{"type":"keyword","ignore_above":1024}}},"ProductFamilyInUncompressedStorage":{"type":"text","fields":{"keyword":{"type":"keyword","ignore_above":256}}},"ProductFamilyInCompressedStorage":{"type":"text","fields":{"keyword":{"type":"keyword","ignore_above":256}}},"PathInUncompressedStorage":{"type":"text","analyzer":"keyword","fields":{"keyword":{"type":"keyword","ignore_above":2048}}},"PathInCompressedStorage":{"type":"text","analyzer":"keyword","fields":{"keyword":{"type":"keyword","ignore_above":2048}}},"EvictionDateInUncompressedStorage":{"type":"date"},"EvictionDateInCompressedStorage":{"type":"date"},"LastInsertionInUncompressedStorage":{"type":"date"},"LastInsertionInCompressedStorage":{"type":"date"},"PersistentInUncompressedStorage":{"type":"boolean"},"PersistentInCompressedStorage":{"type":"boolean"},"AvailableInLta":{"type":"boolean"},"LastModified":{"type":"date"},"LastDataRequest":{"type":"date"}}}}
```

-------------------
