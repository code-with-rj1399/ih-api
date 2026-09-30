const state={base:"",experienceCursor:"",questionCursor:""};

const $=id=>document.getElementById(id);
function baseUrl(){return ($("baseUrl").value||window.location.origin).replace(/\\/$/,"");}
function showRaw(label,data){$("requestLabel").textContent=label;$("rawResponse").textContent=JSON.stringify(data,null,2)}
function toast(message){const el=$("toast");el.textContent=message;el.classList.add("show");setTimeout(()=>el.classList.remove("show"),2400)}
function query(params){return Object.entries(params).filter(([,v])=>v!==undefined&&v!==null&&v!=="").map(([k,v])=>encodeURIComponent(k)+"="+encodeURIComponent(v)).join("&")}
async function request(path){const url=baseUrl()+path;const res=await fetch(url,{headers:{"Accept":"application/json"}});let body;try{body=await res.json()}catch{body={error:{message:await res.text()}}}showRaw((res.ok?"GET ":"GET ") + url,body);if(!res.ok)throw new Error(body?.error?.message||"Request failed ("+res.status+")");return body}
function fmt(value){if(value===null||value===undefined||value==="")return "—";return String(value)}
function field(label,value){return '<div class="field"><b>'+escapeHtml(label)+'</b><span>'+escapeHtml(fmt(value))+'</span></div>'}
function escapeHtml(v){return String(v??"").replace(/[&<>"']/g,c=>({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#039;"}[c]))}
function paginationHtml(p,kind){if(!p)return "";state[kind+"Cursor"]=p.nextCursor||"";return '<div class="meta" style="margin-top:12px"><span>limit: '+fmt(p.limit)+'</span><span>hasMore: '+fmt(p.hasMore)+'</span>'+(p.nextCursor?'<button class="secondary" data-next="'+kind+'">Next page</button>':'')+'</div>'}

async function loadExperiences(){try{
 const path="/api/v1/experiences?"+query({limit:$("expLimit").value,cursor:$("expCursor").value,sort:$("expSort").value,company:$("expCompany").value});
 const data=await request(path), items=data.items||[]; $("experienceList").innerHTML=items.length?items.map(x=>'<article class="card" data-exp-id="'+x.id+'"><h3>'+escapeHtml(x.title||"Untitled experience")+'</h3><div class="meta"><span>'+escapeHtml(fmt(x.company))+'</span><span>'+escapeHtml(fmt(x.role))+'</span><span>'+escapeHtml(fmt(x.level))+'</span><span>'+escapeHtml(fmt(x.location))+'</span></div><div style="margin-top:10px"><span class="chip">'+escapeHtml(fmt(x.sourcePlatform))+'</span> <span class="muted">'+escapeHtml(fmt(x.questionCount))+' questions</span></div></article>').join(""):'<div class="detail empty">No experiences matched these filters.</div>';
 $("experienceList").insertAdjacentHTML("beforeend",paginationHtml(data.pagination,"experience"));
 document.querySelectorAll("[data-exp-id]").forEach(el=>el.onclick=()=>loadExperienceDetail(el.dataset.expId,el));
}catch(e){toast(e.message)}}

async function loadExperienceDetail(id,card){try{
 document.querySelectorAll("[data-exp-id]").forEach(x=>x.classList.remove("selected"));card?.classList.add("selected");
 const data=await request("/api/v1/experiences/"+encodeURIComponent(id)), x=data.item||{};
 $("experienceDetail").classList.remove("empty");
 $("experienceDetail").innerHTML='<h3>'+escapeHtml(fmt(x.title))+'</h3><div class="detail-grid">'+field("ID",x.id)+field("Company",x.company)+field("Role",x.role)+field("Level",x.level)+field("Location",x.location)+field("Source",x.sourcePlatform)+field("Author",x.author)+field("Posted at",x.postedAt)+field("Candidate YoE",x.candidateYoE)+field("Question count",x.questionCount)+field("Original post",x.originalPostUrl)+field("Created at",x.createdAt)+field("Summary",x.summary)+'</div><h4>Questions in this experience</h4><div id="experienceQuestions">Loading…</div>';
 const q=await request("/api/v1/experiences/"+encodeURIComponent(id)+"/questions?limit=100");
 $("experienceQuestions").innerHTML=(q.items||[]).map(item=>'<div class="question-mini" data-q-id="'+item.id+'"><b>'+escapeHtml(fmt(item.questionText))+'</b><div class="meta"><span>'+escapeHtml(fmt(item.company))+'</span><span>'+escapeHtml(fmt(item.role))+'</span><span>'+escapeHtml(fmt(item.sourcePlatform))+'</span></div></div>').join("")||'<span class="muted">No questions found.</span>';
 document.querySelectorAll("#experienceQuestions [data-q-id]").forEach(el=>el.onclick=()=>{document.querySelector('[data-tab="questions"]').click();$("questionDetail").scrollIntoView({behavior:"smooth",block:"start"});loadQuestionDetail(el.dataset.qId)});
}catch(e){toast(e.message)}}

async function loadQuestions(){try{
 const path="/api/v1/questions?"+query({limit:$("qLimit").value,cursor:$("qCursor").value,sort:$("qSort").value,company:$("qCompany").value,type:$("qType").value});
 const data=await request(path),items=data.items||[];$("questionList").innerHTML=items.length?items.map(x=>'<article class="card" data-q-id="'+x.id+'"><h3>'+escapeHtml(x.questionText||"Untitled question")+'</h3><div class="meta"><span>'+escapeHtml(fmt(x.company))+'</span><span>'+escapeHtml(fmt(x.role))+'</span><span>'+escapeHtml(fmt(x.postedAt))+'</span></div><div style="margin-top:10px">'+(x.questionTypes||[]).map(t=>'<span class="chip">'+escapeHtml(t)+'</span> ').join("")+'</div><div class="muted" style="margin-top:8px">confidence: '+escapeHtml(fmt(x.confidence))+'</div></article>').join(""):'<div class="detail empty">No questions matched these filters.</div>';
 $("questionList").insertAdjacentHTML("beforeend",paginationHtml(data.pagination,"question"));
 document.querySelectorAll("#questionList [data-q-id]").forEach(el=>el.onclick=()=>loadQuestionDetail(el.dataset.qId,el));
}catch(e){toast(e.message)}}

async function loadQuestionDetail(id,card){try{
 document.querySelectorAll("#questionList [data-q-id]").forEach(x=>x.classList.remove("selected"));card?.classList.add("selected");
 const data=await request("/api/v1/questions/"+encodeURIComponent(id)),x=data.item||{};$("questionDetail").classList.remove("empty");
 $("questionDetail").innerHTML='<h3>'+escapeHtml(fmt(x.questionText))+'</h3><div class="detail-grid">'+field("ID",x.id)+field("Experience ID",x.experienceId)+field("Types",(x.questionTypes||[]).join(", "))+field("Difficulty",x.difficulty)+field("Confidence",x.confidence)+field("Problem URL",x.problemUrl)+field("Question description",x.questionDescription)+field("Candidate approach",x.candidateApproach)+field("Particularity",x.questionParticularity)+field("Extracted at",x.extractedAt)+field("Created at",x.createdAt)+'</div>';
}catch(e){toast(e.message)}}

async function loadMetadata(endpoint,target){try{const data=await request("/api/v1/meta/"+endpoint+"?limit=100");$(target).innerHTML=(data.items||[]).map(x=>'<span class="tag">'+escapeHtml(x.name)+' <small>'+escapeHtml(x.slug)+'</small></span>').join("")||'<span class="muted">No metadata found.</span>'}catch(e){toast(e.message)}}

document.querySelectorAll(".tab").forEach(tab=>tab.onclick=()=>{document.querySelectorAll(".tab").forEach(x=>x.classList.remove("active"));document.querySelectorAll(".panel").forEach(x=>x.classList.remove("active"));tab.classList.add("active");$(tab.dataset.tab).classList.add("active")});
document.querySelector('[data-action="load-experiences"]').onclick=loadExperiences;
document.querySelector('[data-action="load-questions"]').onclick=loadQuestions;
document.querySelector('[data-action="load-companies"]').onclick=()=>loadMetadata("companies","companies");
document.querySelector('[data-action="load-types"]').onclick=()=>loadMetadata("question-types","questionTypes");
document.querySelector("#saveBase").onclick=()=>{localStorage.setItem("ihApiBase",$("baseUrl").value);toast("API base URL saved")};
document.querySelector("#clearRaw").onclick=()=>{showRaw("No request yet",{})};
document.addEventListener("click",e=>{const kind=e.target.dataset.next;if(!kind)return;if(kind==="experience"){$("expCursor").value=state.experienceCursor;loadExperiences()}else{$("qCursor").value=state.questionCursor;loadQuestions()}});
$("baseUrl").value=localStorage.getItem("ihApiBase")||window.location.origin;
loadExperiences();
