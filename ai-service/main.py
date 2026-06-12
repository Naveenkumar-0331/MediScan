from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from services.pdf_extractor import extract_text_from_pdf
from services.ai_summarizer import generate_summary
from pymongo import MongoClient
from dotenv import load_dotenv
from datetime import datetime
from bson.int64 import Int64
import os

load_dotenv()

app = FastAPI(title="MediScan AI Service", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"]
)

# MongoDB connection
client = MongoClient(os.getenv("MONGODB_URI"))
db = client[os.getenv("MONGODB_DB")]

@app.get("/health")
def health_check():
    return {"status": "ok", "service": "mediscan-ai"}

@app.post("/analyze")
async def analyze_report(
    file: UploadFile = File(...),
    report_id: int = 0,
    patient_id: int = 0
):
    # Step 1 — read file bytes
    file_bytes = await file.read()
    
    # Step 2 — extract text
    if file.content_type == "application/pdf":
        extracted_text = extract_text_from_pdf(file_bytes)
    else:
        raise HTTPException(
            status_code=400, 
            detail="Only PDF supported in Sprint 3"
        )
    
    # Step 3 — generate AI summary
    ai_result = generate_summary(extracted_text)
    
    # Step 4 — save to MongoDB
    doc = {
        "report_id": int(report_id),
        "patient_id": int(patient_id),
        "extracted_text": extracted_text,
        "summary": ai_result["summary"],
        "key_findings": ai_result["key_findings"],
        "abnormal_flags": ai_result["abnormal_flags"],
        "created_at": datetime.utcnow()
    }
    result = db.ai_summaries.insert_one(doc)
    
    # Step 5 — return response
    return {
        "mongo_doc_id": str(result.inserted_id),
        "report_id": report_id,
        "extracted_text": extracted_text[:500],
        "summary": ai_result["summary"],
        "status": "DONE"
    }