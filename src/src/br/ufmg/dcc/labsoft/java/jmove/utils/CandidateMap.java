package br.ufmg.dcc.labsoft.java.jmove.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import br.ufmg.dcc.labsoft.java.jmove.methods.Clazz;
import br.ufmg.dcc.labsoft.java.jmove.suggestion.PreConditionsAnalyser;
import br.ufmg.dcc.labsoft.java.jmove.suggestion.Suggestion;

public class CandidateMap {


	class Candidate{
		public String targetCandidate;
		public Double similarityIndice;
		public Candidate(String targetCandidate, Double similarityIndice){
			this.targetCandidate = targetCandidate;
			this.similarityIndice = similarityIndice;
		}
	}


	Map<IMethod, ArrayList<Candidate>> candidatesRefine;
	ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();

	public CandidateMap() {
		// TODO Auto-generated constructor stub
		this.candidatesRefine = new HashMap<IMethod, ArrayList<Candidate>>();
		this.suggestions = new ArrayList<Suggestion>();

	}

	public void putCandidateOnList(IMethod iMethod, String candidate, Double similarityIndice) {
		// candidatesRefine.get(iMethod);
		// TODO: Include similarityIndice in the candidate list.

		Candidate newCandidate = new Candidate(candidate, similarityIndice);
		if (candidatesRefine.containsKey(iMethod)) {
			ArrayList<Candidate> candidatesList = candidatesRefine.get(iMethod);
			candidatesList.add(newCandidate);
		} else {
			ArrayList<Candidate> candidatesList = new ArrayList<Candidate>();
			candidatesList.add(newCandidate);
			candidatesRefine.put(iMethod, candidatesList);
		}
	}

	public Map<IMethod, ArrayList<Candidate>> getCandidatesRefine() {
		return candidatesRefine;
	}

	public Object[] getCandidates() {

		suggestions = new ArrayList<Suggestion>();

		IWorkbench wb = PlatformUI.getWorkbench();
		IProgressService ps = wb.getProgressService();
		try {
			ps.busyCursorWhile(new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					monitor.beginTask(
							"Checking Move Methods PreConditions for the Suggestions (4/4)",
							candidatesRefine.entrySet().size());

					Suggestion sug;
					Iterator<Entry<IMethod, ArrayList<Candidate>>> it = candidatesRefine
							.entrySet().iterator();

					while (it.hasNext()) {
						monitor.worked(1);
						Entry<IMethod, ArrayList<Candidate>> e = it.next();

						IMethod iMethod = (IMethod) (IMethod) e.getKey();
						ArrayList<Candidate> Candidates = (ArrayList<Candidate>) e
								.getValue();
						PreConditionsAnalyser analyser = new PreConditionsAnalyser();
						if (analyser.methodCanBeMoved(iMethod)) {
							// Checking pre-conditions
							for (Candidate candidate : Candidates) {
								ICompilationUnit clazz = Clazz.getInstance()
										.getICompilation(candidate.targetCandidate);

								if (analyser.satisfiesAllConditions(iMethod,
										clazz)) {
									// passed post-conditions check.
									sug = new Suggestion(iMethod, clazz, candidate.similarityIndice);
									suggestions.add(sug);
									break; // Remove this break to add more suggestions to list??
								}
							}

						}
					}
				}
			});
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// while (it.hasNext()) {
		// Entry<IMethod, ArrayList<String>> e = it.next();
		//
		// IMethod iMethod = (IMethod) (IMethod) e.getKey();
		// ArrayList<String> Candidates = (ArrayList<String>) e.getValue();
		// PreConditionsAnalyser analyser = new PreConditionsAnalyser();
		// if (analyser.methodCanBeMoved(iMethod)) {
		// // checar precondicçoes
		// for (String clazzName : Candidates) {
		// ICompilationUnit clazz = Clazz.getInstance()
		// .getICompilation(clazzName);
		//
		// if (analyser.satisfiesAllConditions(iMethod, clazz)) {
		// sug = new Suggestion(iMethod, clazz);
		// suggestions.add(sug);
		// break;
		// }
		// }
		//
		// }
		// }

		return suggestions.toArray();
	}

	public Object[] getCandidatesWithoutMonitor() {

		suggestions = new ArrayList<Suggestion>();

		Suggestion sug;
		Iterator<Entry<IMethod, ArrayList<Candidate>>> it = candidatesRefine
				.entrySet().iterator();

		while (it.hasNext()) {

			Entry<IMethod, ArrayList<Candidate>> e = it.next();

			IMethod iMethod = (IMethod) (IMethod) e.getKey();
			ArrayList<Candidate> Candidates = (ArrayList<Candidate>) e.getValue();
			PreConditionsAnalyser analyser = new PreConditionsAnalyser();
			if (analyser.methodCanBeMoved(iMethod)) {
				// checar precondicçoes
				for (Candidate candidate : Candidates) {
					ICompilationUnit clazz = Clazz.getInstance()
							.getICompilation(candidate.targetCandidate);

					if (analyser.satisfiesAllConditions(iMethod, clazz)) {
						sug = new Suggestion(iMethod, clazz, candidate.similarityIndice);
						suggestions.add(sug);
						break;
					}
				}

			}
		}

		return suggestions.toArray();
	}
}
