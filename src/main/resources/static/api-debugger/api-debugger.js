const state={experienceCursor:"",questionCursor:""};
const $=id=>document.getElementById(id);
const esc=v=>String(v??"").replace(/[&<>"']/g,c=>({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#039;"}[c]));
const fmt=v=>(v===null||v===undefined||v==="")?"—":String(v);
function baseUrl(){return ($("baseUrl").value||window.location.origin).replace(/\/$/,"")}
function toast(message){const el=$("toast");el.textContent=message;el.classList.add("show");setTimeout(()=>el.classList.remove("show"),2200)}
function query(params){return Object.entries(params).filter(([,v])=>v!==undefined&&v!==null&&v!=="").map(([k,v])=>encodeURIComponent(k)+"="+encodeURIComponent(v)).join("&")}
async function request(path){const res=await fetch(baseUrl()+path,{headers:{"Accept":"application/json"}});let body={};try{body=await res.json()}catch{}if(!res.ok)throw new Error(body?.error?.message||"Request failed ("+res.status+")");return body}
function field(label,value){return '<div class="field"><b>'+esc(label)+'</b><span>'+esc(fmt(value))+'</span></div>'}
function empty(target,message){$(target).innerHTML='<div class="empty">'+esc(message)+'</div>'}
function pagination(target,p,kind){state[kind+"Cursor"]=p?.nextCursor||"";const el=$(target);if(!p?.nextCursor){el.innerHTML=p?'<span class="muted">'+(p.hasMore?"More results available":"End of results")+'</span>':"";return}el.innerHTML='<span class="muted">More results available</span><button class="secondary" data-next="'+kind+'">Next page</button>'}

async function loadExperiences(useCursor=false){
 try{
  const path="/api/v1/experiences?"+query({limit:$("expLimit").value,cursor:useCursor?state.experienceCursor:"",sort:$("expSort").value,company:$("expCompany").value});
  const data=await request(path),items=data.items||[];
  $("experienceList").innerHTML=items.length?items.map(x=>'<article class="experience-card" data-exp-id="'+esc(x.id)+'"><div class="card-title">'+esc(x.title||"Interview experience")+'</div><div class="card-meta"><span>'+esc(fmt(x.company))+'</span><span>'+esc(fmt(x.role))+'</span><span>'+esc(fmt(x.level))+'</span><span>'+esc(fmt(x.location))+'</span></div><div class="question-types" style="margin-top:10px"><span class="chip">'+esc(fmt(x.sourcePlatform))+'</span> <span class="muted">'+esc(fmt(x.questionCount))+' questions</span> <span class="muted">'+esc(fmt(x.postedAt))+'</span></div>'+(x.summary?'<div class="summary">'+esc(x.summary)+'</div>':"")+'</article>').join(""):'<div class="empty">No experiences matched these filters.</div>';
  pagination("experiencePagination",data.pagination,"experience");
  document.querySelectorAll("[data-exp-id]").forEach(el=>el.onclick=()=>loadExperienceDetail(el.dataset.expId,el));
 }catch(e){toast(e.message)}
}

async function loadExperienceDetail(id,card){
 try{
  document.querySelectorAll(".experience-card").forEach(x=>x.classList.remove("selected"));card?.classList.add("selected");
  const data=await request("/api/v1/experiences/"+encodeURIComponent(id)),x=data.item||{};
  const detail=$("experienceDetail");detail.classList.remove("hidden");
  detail.innerHTML='<div class="detail-top"><div><div class="eyebrow">Interview experience</div><h2 class="detail-title">'+esc(x.title||"Interview experience")+'</h2><div class="detail-subtitle">'+esc(fmt(x.company))+' · '+esc(fmt(x.role))+' · '+esc(fmt(x.location))+'</div></div></div><div class="detail-grid">'+field("Company",x.company)+field("Role",x.role)+field("Level",x.level)+field("Location",x.location)+field("Source",x.sourcePlatform)+field("Author",x.author)+field("Posted",x.postedAt)+field("Candidate YoE",x.candidateYoE)+field("Questions",x.questionCount)+'</div>'+(x.summary?'<div class="detail-section"><h3>Summary</h3><div>'+esc(x.summary)+'</div></div>':"")+(x.originalPostUrl?'<div class="detail-section"><a href="'+esc(x.originalPostUrl)+'" target="_blank" rel="noopener noreferrer">Open original interview post</a></div>':"")+'<div class="detail-section"><h3>Questions from this experience</h3><div id="experienceQuestions">Loading questions…</div></div>';
  const q=await request("/api/v1/experiences/"+encodeURIComponent(id)+"/questions?limit=100");
  const items=q.items||[];
  $("experienceQuestions").innerHTML=items.length?items.map(item=>'<div class="detail-question" data-detail-q-id="'+esc(item.id)+'"><div class="detail-question-title">'+esc(item.questionText||item.questionDescription||"Interview question")+'</div><div class="card-meta question-types">'+(item.questionTypes||[]).map(t=>'<span class="chip">'+esc(t)+'</span>').join("")+'</div></div>').join(""):'<span class="muted">No questions found for this experience.</span>';
  document.querySelectorAll("[data-detail-q-id]").forEach(el=>el.onclick=()=>loadQuestionDetail(el.dataset.detailQId));
  detail.scrollIntoView({behavior:"smooth",block:"start"});
 }catch(e){toast(e.message)}
}

async function loadQuestions(useCursor=false){
 try{
  const path="/api/v1/questions?"+query({limit:$("qLimit").value,cursor:useCursor?state.questionCursor:"",sort:$("qSort").value,company:$("qCompany").value,type:$("qType").value});
  const data=await request(path),items=data.items||[];
  $("questionList").innerHTML=items.length?items.map(x=>'<article class="question-card" data-q-id="'+esc(x.id)+'"><div class="card-title">'+esc(x.questionText||x.questionDescription||"Interview question")+'</div><div class="card-meta"><span>'+esc(fmt(x.company))+'</span><span>'+esc(fmt(x.role))+'</span><span>'+esc(fmt(x.postedAt))+'</span></div><div class="question-types">'+(x.questionTypes||[]).map(t=>'<span class="chip">'+esc(t)+'</span> ').join("")+'</div></article>').join(""):'<div class="empty">No questions matched these filters.</div>';
  pagination("questionPagination",data.pagination,"question");
  document.querySelectorAll("[data-q-id]").forEach(el=>el.onclick=()=>loadQuestionDetail(el.dataset.qId,el));
 }catch(e){toast(e.message)}
}

async function loadQuestionDetail(id,card){
 try{
  document.querySelectorAll(".question-card").forEach(x=>x.classList.remove("selected"));card?.classList.add("selected");
  const data=await request("/api/v1/questions/"+encodeURIComponent(id)),x=data.item||{};
  const detail=$("questionDetail");detail.classList.remove("hidden");
  detail.innerHTML='<div class="eyebrow">Interview question</div><h2 class="question-text">'+esc(x.questionText||x.questionDescription||"Interview question")+'</h2><div class="detail-grid">'+field("Company",x.company)+field("Role",x.role)+field("Experience ID",x.experienceId)+field("Types",(x.questionTypes||[]).join(", "))+field("Difficulty",x.difficulty)+field("Confidence",x.confidence)+field("Posted",x.postedAt)+field("Extracted",x.extractedAt)+'</div>'+(x.questionDescription?'<div class="detail-section"><h3>Description</h3><div>'+esc(x.questionDescription)+'</div></div>':"")+(x.candidateApproach?'<div class="detail-section"><h3>Candidate approach</h3><div>'+esc(x.candidateApproach)+'</div></div>':"")+(x.questionParticularity?'<div class="detail-section"><h3>Particularity</h3><div>'+esc(x.questionParticularity)+'</div></div>':"")+(x.problemUrl?'<div class="detail-section"><a href="'+esc(x.problemUrl)+'" target="_blank" rel="noopener noreferrer">Open problem</a></div>':"");
  detail.scrollIntoView({behavior:"smooth",block:"start"});
 }catch(e){toast(e.message)}
}

document.querySelectorAll(".tab").forEach(tab=>tab.onclick=()=>{
 document.querySelectorAll(".tab").forEach(x=>x.classList.remove("active"));
 document.querySelectorAll(".panel").forEach(x=>x.classList.remove("active"));
 tab.classList.add("active");$(tab.dataset.tab).classList.add("active");
});
document.querySelector('[data-action="load-experiences"]').onclick=()=>loadExperiences(false);
document.querySelector('[data-action="load-questions"]').onclick=()=>loadQuestions(false);
$("saveBase").onclick=()=>{localStorage.setItem("ihApiBase",$("baseUrl").value);toast("API base URL saved")};
document.addEventListener("click",e=>{
 const kind=e.target.dataset.next;
 if(kind==="experience")loadExperiences(true);
 if(kind==="question")loadQuestions(true);
});
$("baseUrl").value=localStorage.getItem("ihApiBase")||window.location.origin;
loadExperiences(false);
