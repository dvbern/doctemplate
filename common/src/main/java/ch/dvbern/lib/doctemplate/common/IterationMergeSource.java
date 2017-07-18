/*
 * Copyright 2017 DV Bern AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * limitations under the License.
 */
package ch.dvbern.lib.doctemplate.common;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * MergeSource Implementierung fuer iterative Generierung von Dokumentabschnitten.
 *
 * @author METH
 */
public class IterationMergeSource implements MergeSource {

	/** Logger */
	private static final Log log = LogFactory.getLog(IterationMergeSource.class);

	private List<MergeSource> mergeSources;
	private final MergeSource basicSource;
	private Iterator<MergeSource> sourceIterator;
	private final List<MergeSource> currentSources = new ArrayList<>();

	/**
	 * @param mergeSources ueber diese MergeSourcen wird iteriert
	 * @param vonBisBereich von-/Bis-Bereich in der Form "_SUBvon" oder "_SUBvon_bis"
	 * @param basicSource die uebergeordnete MergeSource
	 */
	public IterationMergeSource(List<MergeSource> mergeSources, String vonBisBereich, MergeSource basicSource) {

		super();

		if (vonBisBereich != null) {
			StringTokenizer st = new StringTokenizer(vonBisBereich.substring(4, vonBisBereich.length()), "_");
			try {
				int von = 0, bis = mergeSources.size();
				von = Integer.parseInt(st.nextToken());
				if (st.hasMoreTokens()) {
					int i = Integer.parseInt(st.nextToken());
					bis = i < bis ? i + 1 : bis; // nicht ueber das Ende hinaus
				}
				if (von < bis && von < mergeSources.size()) {
					this.mergeSources = mergeSources.subList(von, bis);
				} else {
					this.mergeSources = new ArrayList<>();
				}
			} catch (Exception e) {
				throw new RuntimeException("error reading iteration index: " + vonBisBereich, e);
			}
		} else {
			// kein von-Bis-Bereich
			this.mergeSources = mergeSources;
		}

		this.basicSource = basicSource;
	}

	/**
	 * @return <code>true</code> wenn es weitere Iterationssourcen hat
	 */
	public boolean hasNext() {

		if (this.sourceIterator == null) {
			this.sourceIterator = this.mergeSources.iterator();
		}
		return this.sourceIterator.hasNext();
	}

	/**
	 * Initialisiert eine neue Iterations-Schleife, indem <code>currentSources</code> geleert werden.
	 */
	public void next() {

		this.currentSources.clear();

		// mindestens eine SubSource in der Iteration
		// sonst Gefahr eines OutOfMemoryErrors!
		loadCurrentSources(0);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ch.dvbern.rodos.merger.shared.MergeSource#getData(ch.dvbern.rodos.merger.shared. MergeContext,
	 * java.lang.String)
	 */
	@Override
	public Object getData(MergeContext ctx, String key) throws DocTemplateException {

		StringBuffer sbKey = new StringBuffer(key);
		int index = initCurrentSource(sbKey);
		MergeSource currentMergeSource = this.currentSources.get(index);
		return getData(ctx, currentMergeSource, this.basicSource, sbKey.toString());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ch.dvbern.rodos.merger.shared.MergeSource#ifStatement(ch.dvbern.rodos.merger.shared .MergeContext,
	 * java.lang.String)
	 */
	@Override
	public Boolean ifStatement(MergeContext ctx, String key) throws DocTemplateException {

		if (key == null) {
			throw new DocTemplateException("key ist null!");
		}

		if (key.endsWith("hasNext")) {
			return Boolean.valueOf(this.sourceIterator.hasNext());
		}
		StringBuffer sbKey = new StringBuffer(key);
		int index = initCurrentSource(sbKey);
		MergeSource currentMergeSource = this.currentSources.get(index);
		Boolean result = currentMergeSource.ifStatement(ctx, sbKey.toString());
		if (result == null) {
			result = this.basicSource.ifStatement(ctx, sbKey.toString());
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ch.dvbern.rodos.merger.shared.MergeSource#whileStatement(ch.dvbern.rodos.merger. shared.MergeContext,
	 * java.lang.String)
	 */
	@Override
	public List<MergeSource> whileStatement(MergeContext ctx, String key) throws DocTemplateException {

		StringBuffer sbKey = new StringBuffer(key);
		int index = initCurrentSource(sbKey);
		MergeSource currentMergeSource = this.currentSources.get(index);
		List<MergeSource> result = currentMergeSource.whileStatement(ctx, sbKey.toString());
		if (result == null) {
			result = this.basicSource.whileStatement(ctx, sbKey.toString());
		}
		return result;
	}

	private int initCurrentSource(StringBuffer sbKey) {

		int index = 0;
		int p = sbKey.indexOf("_[");
		if (p > 0) {
			try {
				index = Integer.parseInt(sbKey.substring(p + 2, sbKey.length() - 1));
			} catch (Exception e) {
				throw new RuntimeException("error reading mergeField index: " + sbKey.toString(), e);
			}
			sbKey.setLength(p);
		}
		loadCurrentSources(index);
		return index;
	}

	private void loadCurrentSources(int upToIndex) {

		// Initialisierung der SubSources in der Iteration
		while (this.currentSources.size() <= upToIndex) {
			if (hasNext()) {
				this.currentSources.add(this.sourceIterator.next());
			} else {
				this.currentSources.add(new EmptyMergeSource());
			}
		}
	}

	private class EmptyMergeSource implements MergeSource {

		/*
		 * (non-Javadoc)
		 *
		 * @see ch.dvbern.rodos.merger.shared.MergeSource#getData(ch.dvbern.rodos.merger.shared .MergeContext,
		 * java.lang.String)
		 */
		@Override
		public Object getData(MergeContext ctx, String key) throws DocTemplateException {

			return "";
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see ch.dvbern.rodos.merger.shared.MergeSource#ifStatement(ch.dvbern.rodos.merger. shared.MergeContext,
		 * java.lang.String)
		 */
		@Override
		public Boolean ifStatement(MergeContext ctx, String key) throws DocTemplateException {

			return false;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see ch.dvbern.rodos.merger.shared.MergeSource#whileStatement(ch.dvbern.rodos.merger .shared.MergeContext,
		 * java.lang.String)
		 */
		@Override
		public List<MergeSource> whileStatement(MergeContext ctx, String key) throws DocTemplateException {

			return new ArrayList<>();
		}
	}

	/**
	 * MergeSource-Comparator fuer die Sortierung von Listeninhalten.
	 */
	public static class IMSComparator implements Comparator<MergeSource> {

		private final MergeContext ctx;
		private final MergeSource basicSource;
		private final List<String> sortFieldKeys;

		/**
		 * Konstruktor.
		 *
		 * @param ctx
		 * @param basicSource
		 * @param sortFieldKeys
		 */
		public IMSComparator(MergeContext ctx, MergeSource basicSource, List<String> sortFieldKeys) {

			this.ctx = ctx;
			this.basicSource = basicSource;
			this.sortFieldKeys = sortFieldKeys;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		@SuppressWarnings("unchecked")
		public int compare(MergeSource arg0, MergeSource arg1) {

			int result = 0;
			try {
				MergeSource previousMergeSource = this.ctx.getCurrentMergeSource();
				for (Iterator<String> i = this.sortFieldKeys.iterator(); i.hasNext() && result == 0;) {
					String key = i.next();
					boolean desc = false;
					if (key.endsWith("_DESC")) {
						desc = true;
						key = key.substring(0, key.length() - 5);
					}
					this.ctx.setCurrentMergeSource(arg0);
					Object o1 = getData(this.ctx, arg0, this.basicSource, key);
					o1 = o1 != null ? o1 : "";
					this.ctx.setCurrentMergeSource(arg1);
					Object o2 = getData(this.ctx, arg1, this.basicSource, key);
					o2 = o2 != null ? o2 : "";
					if (o1 instanceof Comparable && o1.getClass().equals(o2.getClass())) {
						result = ((Comparable<Object>) o1).compareTo(o2);
					}
					if (desc) {
						result = result * -1;
					}
				}
				this.ctx.setCurrentMergeSource(previousMergeSource);
			} catch (DocTemplateException sfe) {
				log.warn("error in sort comparator", sfe);
			}
			return result;
		}
	}

	/**
	 * Diese statische Methode wird von IterationMergeSource::getData(...) und IMSComparator::compare(...) verwendet.
	 * (Vermeiden von Code-Duplizierung.)
	 */
	private static Object getData(MergeContext ctx, MergeSource primaryMS, MergeSource alternateMS, String key) throws DocTemplateException {

		Object result = primaryMS.getData(ctx, key);
		if (result == null) {
			result = alternateMS.getData(ctx, key);
		}
		return result;
	}
}
