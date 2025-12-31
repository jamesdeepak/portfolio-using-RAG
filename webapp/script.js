// Knowledge Base (Simple RAG Simulation)
const knowledgeBase = [
    {
        questions: ["What services do you offer?", "Services", "What can you do?"],
        answer: "I specialize in **Machine Learning**, **NLP/LLMs**, **Computer Vision**, and **Data Science**. I can build custom AI models, chatbots, and intelligent automation systems for your business."
    },
    {
        questions: ["Can you build a RAG system?", "RAG", "Retrieval Augmented Generation"],
        answer: "Yes! I am an expert in building **Retrieval-Augmented Generation (RAG)** systems. I can connect your custom data (PDFs, Databases) to LLMs like GPT-4 or Llama to create intelligent knowledge assistants."
    },
    {
        questions: ["What is your tech stack?", "Tools", "Technologies"],
        answer: "I work with Python, PyTorch, TensorFlow, LangChain, OpenAI API, and Pinecone for AI. For web apps, I use React, Node.js, and modern cloud architecture."
    },
    {
        questions: ["How can I contact you?", "Contact", "Hire you"],
        answer: "You can reach me via email at **deepak@example.com** or connect with me on LinkedIn using the links in the footer. I'm open to freelance and contract work!"
    }
];

const initialSuggestions = [
    "What services do you offer?",
    "Can you build a RAG system?",
    "What is your tech stack?",
    "How can I contact you?"
];

// DOM Elements
const aiTrigger = document.getElementById('aiTrigger');
const aiWidget = document.getElementById('aiWidget');
const closeAi = document.getElementById('closeAi');
const chatBody = document.getElementById('chatBody');
const suggestionsContainer = document.getElementById('suggestions');

// State
let isOpen = false;

// Toggle Widget
function toggleWidget() {
    isOpen = !isOpen;
    if (isOpen) {
        aiWidget.classList.add('active');
        // Reset if empty (optional, keeping history is better usually)
    } else {
        aiWidget.classList.remove('active');
    }
}

aiTrigger.addEventListener('click', toggleWidget);
closeAi.addEventListener('click', toggleWidget);

// Helper: Create Message
function addMessage(text, sender = 'bot') {
    const div = document.createElement('div');
    div.classList.add('message', sender);

    // Parse Markdown-like bolding (**text**) for simple formatting
    const formattedText = text.replace(/\*\*(.*?)\*\*/g, '<b>$1</b>');
    div.innerHTML = `<p>${formattedText}</p>`;

    chatBody.appendChild(div);
    chatBody.scrollTop = chatBody.scrollHeight;
}

// Helper: Find Answer Locally
function findLocalAnswer(question) {
    const q = question.toLowerCase();

    // Search in knowledgeBase
    for (const entry of knowledgeBase) {
        if (entry.questions.some(k => q.includes(k.toLowerCase()))) {
            return entry.answer;
        }
    }

    // Default fallback
    return "I recommend contacting Deepak directly at **deepak@example.com** for that specific inquiry.";
}

// Logic: Handle Question Click
async function handleQuestion(question) {
    // 1. User Message
    addMessage(question, 'user');

    // 2. Simulate "Thinking"
    const loadingDiv = document.createElement('div');
    loadingDiv.classList.add('message', 'bot');
    loadingDiv.innerHTML = '<i class="fa-solid fa-circle-notch fa-spin"></i> Thinking...';
    chatBody.appendChild(loadingDiv);
    chatBody.scrollTop = chatBody.scrollHeight;

    // 3. Try Fetch from Java Backend, Fallback to Local
    let answer = "";

    try {
        const response = await fetch('/api/ask', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ question: question })
        });

        if (response.ok) {
            const data = await response.json();
            answer = data.answer;
        } else {
            throw new Error("Server error");
        }

    } catch (error) {
        console.warn('Server unreachable, using local knowledge base.', error);
        // Fallback to local logic (Client-side RAG)
        // Simulate a small delay for "thinking" feel
        await new Promise(r => setTimeout(r, 600));
        answer = findLocalAnswer(question);
    }

    // 4. Show Answer
    chatBody.removeChild(loadingDiv);
    addMessage(answer, 'bot');
}

// Initialize Suggestions
function renderSuggestions() {
    suggestionsContainer.innerHTML = '';
    initialSuggestions.forEach(q => {
        const chip = document.createElement('button');
        chip.classList.add('chip');
        chip.innerText = q;
        chip.addEventListener('click', () => handleQuestion(q));
        suggestionsContainer.appendChild(chip);
    });
}

// Run
renderSuggestions();
