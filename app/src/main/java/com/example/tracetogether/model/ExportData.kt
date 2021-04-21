package com.example.tracetogether.model

import com.example.tracetogether.status.persistence.StatusRecord
import com.example.tracetogether.streetpass.persistence.StreetPassRecord

data class ExportData(val recordList: List<StreetPassRecord>, val statusList: List<StatusRecord>)
