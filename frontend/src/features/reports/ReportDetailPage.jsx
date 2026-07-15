import { RotateCw } from 'lucide-react';
import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { getJobDescription } from '../jobDescriptions/jobDescriptionApi.js';
import { getResume } from '../resumes/resumeApi.js';
import { createReport, getReport, retryReport } from './reportApi.js';

const sectionLabels = {
  missingSkills: 'Missing Skills',
  skillGapAnalysis: 'Skill Gap Analysis',
  strengths: 'Strengths',
  weaknesses: 'Weaknesses',
  resumeImprovementSuggestions: 'Resume Improvement Suggestions',
  missingKeywords: 'Missing Keywords',
  resumeRewriteSuggestions: 'Resume Rewrite Suggestions',
  technicalInterviewQuestions: 'Technical Interview Questions',
  behavioralQuestions: 'Behavioral Questions',
  projectBasedQuestions: 'Project-Based Questions',
  systemDesignQuestions: 'System Design Questions',
  hrQuestions: 'HR Questions',
  salaryNegotiationTips: 'Salary Negotiation Tips',
  twoWeekPreparationPlan: 'Two-Week Preparation Plan',
  learningResources: 'Learning Resources'
};

export function ReportDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [report, setReport] = useState(null);
  const [resume, setResume] = useState(null);
  const [jobDescription, setJobDescription] = useState(null);
  const [isRegenerating, setIsRegenerating] = useState(false);

  useEffect(() => {
    let active = true;
    let timer;
    const load = async () => {
      const data = await getReport(id);
      if (!active) return;
      setReport(data);
      if (data.status === 'PENDING' || data.status === 'PROCESSING') {
        timer = setTimeout(load, 1500);
      }
    };
    load();
    return () => {
      active = false;
      clearTimeout(timer);
    };
  }, [id]);

  useEffect(() => {
    let active = true;
    setResume(null);
    setJobDescription(null);

    if (!report?.resumeId || !report?.jobDescriptionId) {
      return () => {
        active = false;
      };
    }

    Promise.all([getResume(report.resumeId), getJobDescription(report.jobDescriptionId)])
      .then(([resumeData, jobDescriptionData]) => {
        if (!active) return;
        setResume(resumeData);
        setJobDescription(jobDescriptionData);
      })
      .catch(() => {});

    return () => {
      active = false;
    };
  }, [report?.resumeId, report?.jobDescriptionId]);

  const onRetry = async () => {
    const next = await retryReport(report.id);
    setReport(next);
  };

  const onRegenerate = async () => {
    if (!report?.resumeId || !report?.jobDescriptionId) return;
    setIsRegenerating(true);
    try {
      const next = await createReport({
        resumeId: report.resumeId,
        jobDescriptionId: report.jobDescriptionId
      });
      navigate(`/reports/${next.id}`);
    } finally {
      setIsRegenerating(false);
    }
  };

  if (!report) {
    return <p className="text-secondary">Loading report...</p>;
  }

  if (report.status === 'PENDING' || report.status === 'PROCESSING') {
    return (
      <section className="rounded-xl border border-border bg-card p-8 text-center shadow-sm">
        <RotateCw className="mx-auto animate-spin text-accent" />
        <h1 className="mt-4 text-2xl font-semibold">Generating report</h1>
        <p className="mt-2 text-secondary">Status: {report.status}</p>
      </section>
    );
  }

  if (report.status === 'FAILED') {
    return (
      <section className="rounded-xl border border-accent/30 bg-card p-8 shadow-sm">
        <h1 className="text-2xl font-semibold text-accent">Report failed</h1>
        <p className="mt-2 text-secondary">{report.failureReason}</p>
        <button className="mt-5 rounded-lg bg-accent px-4 py-2 font-medium text-white hover:bg-accentHover" onClick={onRetry} type="button">
          Retry
        </button>
      </section>
    );
  }

  const payload = report.payload ?? {};
  const hasKnownReportContent = Boolean(payload.finalSummary) || Object.keys(sectionLabels).some((key) => hasSectionContent(payload[key]));
  const unknownPayload = getUnknownPayload(payload);
  return (
    <section>
      <div className="mb-6">
        <h1 className="text-3xl font-semibold text-primary">Report #{report.id}</h1>
        <div className="mt-4 flex flex-wrap gap-3">
          <span className="rounded-lg bg-accent/10 px-4 py-2 font-semibold text-accent">ATS Match {report.atsMatchScore}</span>
          <span className="rounded-lg bg-accent/10 px-4 py-2 font-semibold text-accent">Readiness {report.interviewReadinessScore}</span>
          <Link className="rounded-lg bg-accent px-4 py-2 font-semibold text-white hover:bg-accentHover" to={`/reports/${report.id}/chat`}>
            Chat about this report
          </Link>
          <button className="rounded-lg border border-border bg-card px-4 py-2 font-semibold text-secondary hover:bg-sidebar disabled:cursor-not-allowed disabled:opacity-60" disabled={isRegenerating} onClick={onRegenerate} type="button">
            {isRegenerating ? 'Regenerating...' : 'Regenerate report'}
          </button>
        </div>
      </div>
      <div className="space-y-4">
        <h2 className="text-xl font-semibold text-primary">Generated Report</h2>
        {payload.finalSummary && (
          <article className="rounded-xl border border-border bg-card p-5 shadow-sm">
            <h2 className="font-semibold">Final Summary</h2>
            <p className="mt-2 whitespace-pre-wrap text-sm leading-6 text-secondary">{formatPayloadValue(payload.finalSummary)}</p>
          </article>
        )}
        {Object.entries(sectionLabels).map(([key, label]) => (
          <ReportSection items={payload[key]} key={key} title={label} />
        ))}
        {!hasKnownReportContent && (
          <article className="rounded-xl border border-accent/30 bg-card p-5 shadow-sm">
            <h2 className="font-semibold text-accent">AI output needs review</h2>
            <p className="mt-2 text-sm leading-6 text-secondary">
              The report finished, but the AI response did not match the expected report fields. The saved source inputs are below.
            </p>
          </article>
        )}
        {unknownPayload && (
          <article className="rounded-xl border border-border bg-card p-5 shadow-sm">
            <h2 className="font-semibold">Additional AI Output</h2>
            <pre className="mt-3 max-h-96 overflow-auto whitespace-pre-wrap rounded-lg bg-paper p-4 text-sm leading-6 text-primary">{unknownPayload}</pre>
          </article>
        )}
        <SourceInputs resume={resume} jobDescription={jobDescription} />
      </div>
    </section>
  );
}

function ReportSection({ title, items }) {
  const values = normalizeSectionItems(items);
  if (values.length === 0) return null;
  return (
    <article className="rounded-xl border border-border bg-card p-5 shadow-sm">
      <h2 className="font-semibold">{title}</h2>
      <ul className="mt-3 list-disc space-y-2 pl-5 text-sm leading-6 text-secondary">
        {values.map((item, index) => (
          <li className="whitespace-pre-wrap" key={`${title}-${index}`}>
            {item}
          </li>
        ))}
      </ul>
    </article>
  );
}

function hasSectionContent(value) {
  return normalizeSectionItems(value).length > 0;
}

function normalizeSectionItems(value) {
  if (value == null) return [];
  if (Array.isArray(value)) {
    return value.map(formatPayloadValue).filter(Boolean);
  }
  return [formatPayloadValue(value)].filter(Boolean);
}

function formatPayloadValue(value) {
  if (value == null) return '';
  if (typeof value === 'string') return value.trim();
  if (typeof value === 'number' || typeof value === 'boolean') return String(value);
  return JSON.stringify(value, null, 2);
}

function getUnknownPayload(payload) {
  if (!payload || typeof payload !== 'object' || Array.isArray(payload)) return null;
  const knownKeys = new Set(['finalSummary', 'atsMatchScore', 'interviewReadinessScore', ...Object.keys(sectionLabels)]);
  const unknown = Object.fromEntries(Object.entries(payload).filter(([key, value]) => !knownKeys.has(key) && value != null));
  return Object.keys(unknown).length > 0 ? JSON.stringify(unknown, null, 2) : null;
}

function SourceInputs({ resume, jobDescription }) {
  if (!resume && !jobDescription) return null;
  return (
    <section className="grid gap-4 lg:grid-cols-2">
      {resume && (
        <article className="rounded-xl border border-border bg-card p-5 shadow-sm">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <h2 className="font-semibold">Resume Used</h2>
            <span className="rounded-lg bg-accent/10 px-3 py-1 text-sm font-medium text-accent">{resume.parseStatus}</span>
          </div>
          <p className="mt-2 break-words text-sm text-secondary">{resume.originalFilename}</p>
          <pre className="mt-4 max-h-96 overflow-auto whitespace-pre-wrap rounded-lg bg-paper p-4 text-sm leading-6 text-primary">{resume.parsedText || 'No parsed resume text available.'}</pre>
        </article>
      )}
      {jobDescription && (
        <article className="rounded-xl border border-border bg-card p-5 shadow-sm">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <h2 className="font-semibold">Job Description Used</h2>
            <span className="rounded-lg bg-accent/10 px-3 py-1 text-sm font-medium text-accent">{jobDescription.parseStatus}</span>
          </div>
          <p className="mt-2 text-sm text-secondary">
            {jobDescription.roleTitle || 'Untitled role'} at {jobDescription.companyName || 'Company not set'}
          </p>
          <pre className="mt-4 max-h-96 overflow-auto whitespace-pre-wrap rounded-lg bg-paper p-4 text-sm leading-6 text-primary">{jobDescription.rawText || 'No job description text available.'}</pre>
        </article>
      )}
    </section>
  );
}
