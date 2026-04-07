# ☁️ AWS Serverless File Handler

A cloud-native serverless application built using AWS CDK (Java) to process and manage file uploads in a scalable, event-driven architecture.

---

## 🚀 Overview
This project demonstrates a production-style serverless pipeline that handles file ingestion, processing, and storage using AWS services. It follows Infrastructure as Code (IaC) principles to enable scalable, repeatable, and automated deployments.

---

## 🏗️ Architecture
- File uploaded to Amazon S3  
- Event triggers AWS Lambda function  
- Processing logic executed serverlessly  
- Infrastructure provisioned using AWS CDK  

---

## 🛠️ Main Tech Stack
- AWS CDK (Java)  
- AWS Lambda  
- Amazon S3  
- CloudFormation  
- Maven  

---

## ⚙️ Features
- Event-driven file processing pipeline  
- Serverless architecture for scalability and cost efficiency  
- Infrastructure automation using IaC  
- Fault-tolerant and extensible design  

---

## 📦 Setup & Deployment

```bash
mvn package
cdk synth
cdk deploy
