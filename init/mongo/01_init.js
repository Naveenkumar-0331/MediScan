// ============================================================
//  MediScan AI — MongoDB Init Script
//  Creates collections + indexes on first container start
// ============================================================

db = db.getSiblingDB('mediscan_docs');

// ----------------------------------------------------------
// extracted_texts — raw text pulled from uploaded reports
// ----------------------------------------------------------
db.createCollection('extracted_texts', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['report_id', 'raw_text', 'created_at'],
      properties: {
        report_id:     { bsonType: 'long',   description: 'FK to MySQL reports.id' },
        patient_id:    { bsonType: 'long' },
        raw_text:      { bsonType: 'string', description: 'Full extracted text' },
        pii_masked:    { bsonType: 'bool',   description: 'True after Presidio PII scan' },
        page_count:    { bsonType: 'int' },
        language:      { bsonType: 'string' },
        extraction_method: { bsonType: 'string', enum: ['pdfplumber', 'easyocr', 'manual'] },
        created_at:    { bsonType: 'date' }
      }
    }
  }
});

db.extracted_texts.createIndex({ report_id: 1 }, { unique: true });
db.extracted_texts.createIndex({ patient_id: 1 });
db.extracted_texts.createIndex({ created_at: -1 });

// ----------------------------------------------------------
// ai_summaries — LLM-generated summaries + trend analysis
// ----------------------------------------------------------
db.createCollection('ai_summaries', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['report_id', 'summary', 'created_at'],
      properties: {
        report_id:      { bsonType: 'long' },
        patient_id:     { bsonType: 'long' },
        summary:        { bsonType: 'string', description: 'LLM-generated summary' },
        key_findings:   { bsonType: 'array',  description: 'List of flagged values' },
        abnormal_flags: { bsonType: 'array',  description: 'Out-of-range markers' },
        trend_data:     { bsonType: 'object', description: 'Comparison with past reports' },
        model_used:     { bsonType: 'string' },
        cached:         { bsonType: 'bool',   description: 'True if served from Redis' },
        created_at:     { bsonType: 'date' }
      }
    }
  }
});

db.ai_summaries.createIndex({ report_id: 1 }, { unique: true });
db.ai_summaries.createIndex({ patient_id: 1 });
db.ai_summaries.createIndex({ created_at: -1 });

// ----------------------------------------------------------
// audit_events — detailed audit trail (MongoDB for flex schema)
// ----------------------------------------------------------
db.createCollection('audit_events');
db.audit_events.createIndex({ user_id: 1 });
db.audit_events.createIndex({ patient_id: 1 });
db.audit_events.createIndex({ action: 1 });
db.audit_events.createIndex({ timestamp: -1 });
// Auto-expire audit events after 2 years (GDPR / DPDPA retention)
db.audit_events.createIndex({ timestamp: 1 }, { expireAfterSeconds: 63072000 });

// ----------------------------------------------------------
// Sample document shapes (for reference)
// ----------------------------------------------------------

// audit_events sample:
// {
//   user_id:    123,
//   patient_id: 456,
//   action:     "REPORT_VIEW",
//   resource:   "reports/42",
//   ip_address: "192.168.1.10",
//   user_agent: "Mozilla/5.0...",
//   metadata:   { report_type: "LAB", doctor_name: "Dr. Sharma" },
//   timestamp:  ISODate("2024-01-15T10:30:00Z")
// }

print('MediScan MongoDB collections and indexes created successfully');
