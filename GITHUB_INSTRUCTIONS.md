# How to Add This Project to Your GitHub

This document provides a clear, step-by-step guide to uploading this **Student Attendance System** project to your personal GitHub repository.

---

## Prerequisites
1. **GitHub Account**: Make sure you have a registered account on [GitHub](https://github.com).
2. **Git Installed**: Ensure Git is installed on your machine.
   - You can check by opening your terminal (CMD / PowerShell / Bash) and typing:
     ```bash
     git --version
     ```
   - If not installed, download it from [git-scm.com](https://git-scm.com/) and follow the default installation prompts.

---

## Step 1: Open Your Terminal
Open your terminal and navigate to the project root directory:
```powershell
cd "C:\Users\khann\.gemini\antigravity\scratch\student-attendance-system"
```

---

## Step 2: Initialize Your Local Git Repository
Run the following command to initialize this folder as a Git repository:
```bash
git init
```
*This creates a hidden `.git` folder in your project directory to track file changes.*

---

## Step 3: Stage and Commit the Files
We have already created a `.gitignore` file for you. It ensures that temporary H2 database files, target folders (compiled classes), and IDE configurations are not uploaded.

1. **Stage all files** for the first commit:
   ```bash
   git add .
   ```
2. **Commit the files** with a descriptive message:
   ```bash
   git commit -m "Initial commit: Student Attendance System with JWT and Leaves Management"
   ```

---

## Step 4: Create a New Repository on GitHub
1. Log in to [GitHub](https://github.com).
2. In the top-right corner of the page, click the **`+`** icon and select **New repository**.
3. Fill in the repository details:
   - **Repository name**: `student-attendance-system`
   - **Description** (optional): `Role-based Student Attendance System using Spring Boot, Security, JWT, and Thymeleaf.`
   - **Public/Private**: Choose whichever you prefer.
4. **CRITICAL**: Do **NOT** check "Add a README file", "Add .gitignore", or "Choose a license". (We already have these files locally; checking them will cause a conflict).
5. Click the green **Create repository** button.

---

## Step 5: Link Local Repository to GitHub and Push
Once the repository is created, GitHub will show a page with several commands under the heading **"…or push an existing repository from the command line"**.

Copy and run those commands in your terminal:

1. **Rename your default branch to `main`**:
   ```bash
   git branch -M main
   ```
2. **Add your GitHub repository as the remote origin** (Replace `<your-github-username>` with your actual GitHub username):
   ```bash
   git remote add origin https://github.com/<your-github-username>/student-attendance-system.git
   ```
3. **Push the code** to GitHub:
   ```bash
   git push -u origin main
   ```

---

## How to Make Updates in the Future
Whenever you make changes to the code and want to push them to GitHub:
1. Stage the modified files:
   ```bash
   git add .
   ```
2. Commit the changes:
   ```bash
   git commit -m "Describe your changes here"
   ```
3. Push to GitHub:
   ```bash
   git push
   ```
