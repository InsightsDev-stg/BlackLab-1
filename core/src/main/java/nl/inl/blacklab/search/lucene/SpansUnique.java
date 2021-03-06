/*******************************************************************************
 * Copyright (c) 2010, 2012 Institute for Dutch Lexicology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package nl.inl.blacklab.search.lucene;

import java.io.IOException;

import org.apache.lucene.search.spans.Spans;

import nl.inl.blacklab.search.Span;

/**
 * Remove consecutive duplicate hits from a source spans.
 */
public class SpansUnique extends BLSpans {

	private BLSpans src;

	private int currentDoc = -1;

	private int currentStart = Spans.NO_MORE_POSITIONS;

	public SpansUnique(Spans src) {
		this.src = BLSpansWrapper.optWrapSort(src);
	}

	@Override
	public int docID() {
		return currentDoc;
	}

	@Override
	public int startPosition() {
		return currentStart;
	}

	@Override
	public int endPosition() {
		return src.endPosition();
	}

	@Override
	public int nextDoc() throws IOException {
		if (currentDoc != NO_MORE_DOCS) {
			currentDoc = src.nextDoc();
			currentStart = -1;
		}
		return currentDoc;
	}

	@Override
	public int nextStartPosition() throws IOException {
		int prevStart, prevEnd;
		if (currentStart != NO_MORE_POSITIONS) {
			do {
				prevStart = currentStart;
				prevEnd = src.endPosition();
				currentStart = src.nextStartPosition();
			} while (prevStart == currentStart && prevEnd == src.endPosition());
		}
		return currentStart;
	}

	@Override
	public int advanceStartPosition(int target) throws IOException {
		int prevStart, prevEnd;
		if (currentStart != NO_MORE_POSITIONS) {
			prevStart = currentStart;
			prevEnd = src.endPosition();
			currentStart = src.advanceStartPosition(target);
			while (prevStart == currentStart && prevEnd == src.endPosition()) {
				prevStart = currentStart;
				prevEnd = src.endPosition();
				currentStart = src.nextStartPosition();
			}
		}
		return currentStart;
	}

	@Override
	public int advance(int target) throws IOException {
		if (currentDoc != NO_MORE_DOCS) {
			if (target > currentDoc) {
				// Skip to the target doc
				currentDoc = src.advance(target);
				currentStart = -1;
			} else {
				// We're already in or past the target doc. Just go to the next doc.
				nextDoc();
			}
		}
		return currentDoc;
	}

	@Override
	public String toString() {
		return "UniqueSpans(" + src.toString() + ")";
	}

	@Override
	public boolean hitsEndPointSorted() {
		return src.hitsEndPointSorted();
	}

	@Override
	public boolean hitsStartPointSorted() {
		return true;
	}

	@Override
	public boolean hitsAllSameLength() {
		return src.hitsAllSameLength();
	}

	@Override
	public int hitsLength() {
		return src.hitsLength();
	}

	@Override
	public boolean hitsHaveUniqueStart() {
		return src.hitsHaveUniqueStart();
	}

	@Override
	public boolean hitsHaveUniqueEnd() {
		return src.hitsHaveUniqueEnd();
	}

	@Override
	public boolean hitsAreUnique() {
		return true;
	}

	@Override
	public void passHitQueryContextToClauses(HitQueryContext context) {
		src.setHitQueryContext(context);
	}

	@Override
	public void getCapturedGroups(Span[] capturedGroups) {
		if (!childClausesCaptureGroups)
			return;
		src.getCapturedGroups(capturedGroups);
	}

}
