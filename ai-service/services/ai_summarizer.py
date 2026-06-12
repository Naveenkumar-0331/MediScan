from langchain_groq import ChatGroq
from langchain_core.prompts import ChatPromptTemplate
from dotenv import load_dotenv
import os

load_dotenv()

llm = ChatGroq(
    api_key=os.getenv("GROQ_API_KEY"),
    model="llama-3.1-8b-instant",
    temperature=0.3
)

prompt = ChatPromptTemplate.from_messages([
    ("system", """You are a medical AI assistant. 
    Analyze the medical report text and provide:
    1. A clear summary in simple language
    2. Key findings (list the important values)
    3. Abnormal values (flag anything outside normal range)
    4. Recommendations (what the doctor should note)
    Keep it concise and professional."""),
    ("human", "Medical Report:\n{report_text}")
])

chain = prompt | llm

def generate_summary(report_text: str) -> dict:
    if not report_text or len(report_text.strip()) < 10:
        return {
            "summary": "Could not extract meaningful text from report",
            "key_findings": [],
            "abnormal_flags": [],
            "recommendations": "Please ensure report is readable"
        }
    
    response = chain.invoke({"report_text": report_text[:3000]})
    
    return {
        "summary": response.content,
        "key_findings": [],
        "abnormal_flags": [],
        "recommendations": ""
    }