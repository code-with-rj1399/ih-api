(() => {
  const state = { experienceCursor: "", questionCursor: "" };

  const $ = (id) => document.getElementById(id);
  const q = (selector) => document.querySelector(selector);

  const esc = (value) =>
    String(value ?? "").replace(/[&<>"']/g, (char) => ({
      "&": "&amp;",
      "<": "&lt;",
      ">": "&gt;",
      '"': "&quot;",
      "'": "&#039;"
    }[char]));

  const fmt = (value) =>
    value === null || value === undefined || value === "" ? "—" : String(value);

  const api = (path) => window.location.origin + path;

  function setStatus(ok, message) {
    $("apiDot").className = ok ? "ok" : "error";
    $("apiState").textContent = message;
  }

  function showMessage(id, message) {
    const element = $(id);
    element.textContent = message;
    element.classList.remove("hidden");
  }

  function clearMessage(id) {
    $(id).classList.add("hidden");
  }

  async function request(path) {
    const response = await fetch(api(path), {
      headers: { Accept: "application/json" }
    });

    let body = null;
    try {
      body = await response.json();
    } catch (_) {}

    if (!response.ok) {
      setStatus(false, "API error " + response.status);
      throw new Error(body?.error?.message || "Request failed (" + response.status + ")");
    }

    setStatus(true, "API connected");
    return body;
  }

  function query(params) {
    return Object.entries(params)
      .filter(([, value]) => value !== undefined && value !== null && value !== "")
      .map(([key, value]) => encodeURIComponent(key) + "=" + encodeURIComponent(value))
      .join("&");
  }

  function renderPagination(targetId, pagination, kind) {
    const element = $(targetId);
    state[kind + "Cursor"] = pagination?.nextCursor || "";
    element.innerHTML = pagination?.nextCursor
      ? '<span>More results available</span><button class="secondary" data-next="' + kind + '">Next</button>'
      : "<span>End of results</span>";
  }

  function renderTypes(types) {
    return (types || [])
      .map((type) => '<span class="pill">' + esc(type) + "</span>")
      .join("");
  }

  async function loadExperiences(next = false) {
    clearMessage("experienceMessage");

    try {
      const path = "/api/v1/experiences?" + query({
        limit: $("expLimit").value,
        cursor: next ? state.experienceCursor : "",
        sort: $("expSort").value,
        company: $("expCompany").value.trim()
      });

      const data = await request(path);
      const rows = data?.items || [];

      $("experienceRows").innerHTML = rows.length
        ? rows.map((item) => `
            <tr data-experience-id="${esc(item.id)}">
              <td>
                <div class="primary-text">${esc(item.title || "Interview experience")}</div>
                <div class="sub-text">#${esc(item.id)}</div>
              </td>
              <td>${esc(fmt(item.company))}</td>
              <td>${esc(fmt(item.role))}</td>
              <td><span class="pill">${esc(fmt(item.sourcePlatform))}</span></td>
              <td>${esc(fmt(item.postedAt))}</td>
              <td>${esc(fmt(item.questionCount))}</td>
            </tr>`).join("")
        : '<tr><td colspan="6" class="empty">No experiences found.</td></tr>';

      renderPagination("experiencePagination", data?.pagination, "experience");

      document.querySelectorAll("[data-experience-id]").forEach((row) => {
        row.addEventListener("click", () => loadExperienceDetail(row.dataset.experienceId));
      });
    } catch (error) {
      showMessage("experienceMessage", error.message);
    }
  }

  async function loadExperienceDetail(id) {
    try {
      const data = await request("/api/v1/experiences/" + encodeURIComponent(id));
      const item = data?.item || {};
      const detail = $("experienceDetail");

      detail.classList.remove("hidden");
      detail.innerHTML = `
        <div class="drawer-head">
          <div>
            <div class="eyebrow">Experience #${esc(id)}</div>
            <h2>${esc(item.title || "Interview experience")}</h2>
            <p>${esc(fmt(item.company))} · ${esc(fmt(item.role))}</p>
          </div>
          <button class="icon-button" data-close="experienceDetail" aria-label="Close">×</button>
        </div>

        <div class="detail-grid">
          ${field("Company", item.company)}
          ${field("Role", item.role)}
          ${field("Level", item.level)}
          ${field("Location", item.location)}
          ${field("Source", item.sourcePlatform)}
          ${field("Author", item.author)}
          ${field("Posted", item.postedAt)}
          ${field("Questions", item.questionCount)}
        </div>

        ${item.summary ? '<div class="detail-section"><h3>Summary</h3><p>' + esc(item.summary) + "</p></div>" : ""}

        <div class="detail-section">
          <h3>Questions from this experience</h3>
          <div id="experienceQuestions" class="question-list">Loading…</div>
        </div>
      `;

      const questions = await request(
        "/api/v1/experiences/" + encodeURIComponent(id) + "/questions?limit=100"
      );

      const items = questions?.items || [];
      $("experienceQuestions").innerHTML = items.length
        ? items.map((question) => `
            <button class="question-item" data-question-id="${esc(question.id)}">
              <span>${esc(question.questionText || "Interview question")}</span>
              <span>${renderTypes(question.questionTypes)}</span>
            </button>`).join("")
        : '<p class="muted">No questions found.</p>';

      document.querySelectorAll("[data-question-id]").forEach((row) => {
        row.addEventListener("click", () => loadQuestionDetail(row.dataset.questionId));
      });

      detail.scrollIntoView({ behavior: "smooth", block: "start" });
    } catch (error) {
      showMessage("experienceMessage", error.message);
    }
  }

  async function loadQuestions(next = false) {
    clearMessage("questionMessage");

    try {
      const path = "/api/v1/questions?" + query({
        limit: $("qLimit").value,
        cursor: next ? state.questionCursor : "",
        sort: $("qSort").value,
        company: $("qCompany").value.trim(),
        type: $("qType").value.trim()
      });

      const data = await request(path);
      const rows = data?.items || [];

      $("questionRows").innerHTML = rows.length
        ? rows.map((item) => `
            <tr data-question-id="${esc(item.id)}">
              <td>
                <div class="primary-text">${esc(item.questionText || "Interview question")}</div>
                <div class="sub-text">#${esc(item.id)}</div>
              </td>
              <td>${esc(fmt(item.company))}</td>
              <td>${esc(fmt(item.role))}</td>
              <td>${renderTypes(item.questionTypes)}</td>
              <td>${esc(fmt(item.postedAt))}</td>
              <td>${esc(fmt(item.experienceId))}</td>
            </tr>`).join("")
        : '<tr><td colspan="6" class="empty">No questions found.</td></tr>';

      renderPagination("questionPagination", data?.pagination, "question");

      document.querySelectorAll("[data-question-id]").forEach((row) => {
        row.addEventListener("click", () => loadQuestionDetail(row.dataset.questionId));
      });
    } catch (error) {
      showMessage("questionMessage", error.message);
    }
  }

  async function loadQuestionDetail(id) {
    try {
      const data = await request("/api/v1/questions/" + encodeURIComponent(id));
      const item = data?.item || {};
      const detail = $("questionDetail");

      detail.classList.remove("hidden");
      detail.innerHTML = `
        <div class="drawer-head">
          <div>
            <div class="eyebrow">Question #${esc(id)}</div>
            <h2>${esc(item.questionText || "Interview question")}</h2>
          </div>
          <button class="icon-button" data-close="questionDetail" aria-label="Close">×</button>
        </div>

        <div class="detail-grid">
          ${field("Experience ID", item.experienceId)}
          ${field("Types", (item.questionTypes || []).join(", "))}
          ${field("Difficulty", item.difficulty)}
          ${field("Confidence", item.confidence)}
          ${field("Extracted", item.extractedAt)}
          ${field("Created", item.createdAt)}
        </div>

        ${item.questionDescription ? '<div class="detail-section"><h3>Description</h3><p>' + esc(item.questionDescription) + "</p></div>" : ""}
        ${item.candidateApproach ? '<div class="detail-section"><h3>Candidate approach</h3><p>' + esc(item.candidateApproach) + "</p></div>" : ""}
        ${item.problemUrl ? '<div class="detail-section"><h3>Problem URL</h3><p>' + esc(item.problemUrl) + "</p></div>" : ""}
      `;

      detail.scrollIntoView({ behavior: "smooth", block: "start" });
    } catch (error) {
      showMessage("questionMessage", error.message);
    }
  }

  function field(label, value) {
    return `
      <div class="detail-field">
        <span>${esc(label)}</span>
        <strong>${esc(fmt(value))}</strong>
      </div>`;
  }

  function activateTab(tab) {
    document.querySelectorAll(".side-link").forEach((button) => {
      button.classList.toggle("active", button.dataset.tab === tab);
    });

    document.querySelectorAll(".panel").forEach((panel) => {
      panel.classList.toggle("active", panel.id === tab);
    });
  }

  document.querySelectorAll(".side-link").forEach((button) => {
    button.addEventListener("click", () => activateTab(button.dataset.tab));
  });

  q('[data-action="load-experiences"]').addEventListener("click", () => {
    state.experienceCursor = "";
    loadExperiences();
  });

  q('[data-action="load-questions"]').addEventListener("click", () => {
    state.questionCursor = "";
    loadQuestions();
  });

  q('[data-action="clear-exp"]').addEventListener("click", () => {
    $("expCompany").value = "";
    $("expSort").value = "newest";
    state.experienceCursor = "";
    loadExperiences();
  });

  q('[data-action="clear-q"]').addEventListener("click", () => {
    $("qCompany").value = "";
    $("qType").value = "";
    $("qSort").value = "newest";
    state.questionCursor = "";
    loadQuestions();
  });

  document.addEventListener("click", (event) => {
    const next = event.target.closest("[data-next]")?.dataset.next;
    if (next === "experience") loadExperiences(true);
    if (next === "question") loadQuestions(true);

    const close = event.target.closest("[data-close]")?.dataset.close;
    if (close) $(close).classList.add("hidden");
  });

  loadExperiences();
})();