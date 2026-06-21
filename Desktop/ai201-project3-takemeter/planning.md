planning.md
Project Title

TakeMeter: Evaluating NBA Discourse Quality in r/nba

1. Community
Chosen Community

I selected the NBA Reddit community, specifically r/nba.

Why This Community?

r/nba contains millions of users discussing games, trades, player performances, championships, injuries, and league news. The community contains a wide range of discourse styles, from detailed statistical analysis to emotional reactions and bold hot takes. This variety makes it an ideal environment for a text classification project.

2. Labels
Analysis

Posts that support a basketball argument using evidence, statistics, strategic reasoning, or historical comparisons.

Examples:

"The Knicks' defense improved because they reduced opponent corner-three attempts."
"Brunson's playoff efficiency exceeds most guards due to his low turnover percentage."
Hot Take

Posts expressing strong opinions with little or no supporting evidence.

Examples:

"Wembanyama will become the greatest player in NBA history."
"Brunson is already the greatest Knick of all time."
Reaction

Posts expressing emotional responses to games, highlights, trades, or events.

Examples:

"THE NEW YORK KNICKS ARE YOUR 2026 NBA CHAMPIONS!"
"WHAT A SHOT!"
News / Information

Posts primarily sharing factual information, announcements, transactions, injuries, or quotes.

Examples:

"CJ McCollum signs a one-year extension with Atlanta."
"The Knicks defeated the Spurs 94-90 to win the championship."
3. Hard Edge Cases
Example

"LeBron is overrated because he went 4-6 in the Finals."

Possible Labels:

Analysis
Hot Take
Decision Rule

If evidence is used to build a structured argument, classify as Analysis.

If evidence is only used to support a provocative opinion without deeper reasoning, classify as Hot Take.

4. Data Collection Plan
Source

Reddit r/nba posts and comments.

Dataset Size

At least 200 examples.

Target Distribution
Label	Target
Analysis	50
Hot Take	50
Reaction	50
News	50
If Labels Become Imbalanced

If one category contains significantly fewer examples, additional examples will be collected specifically for that category until a more balanced dataset is achieved.

5. Evaluation Metrics
Accuracy

Measures overall percentage of correctly classified examples.

Precision

Measures how often predicted labels are correct.

Recall

Measures how many true examples of each class are successfully identified.

F1 Score

Balances precision and recall and is particularly useful if label frequencies become uneven.

Confusion Matrix

Shows which labels are most commonly confused by the model.

Accuracy alone is insufficient because a model may perform well overall while performing poorly on specific labels.

6. Definition of Success

The classifier will be considered successful if:

Test Accuracy ≥ 75%
Average F1 Score ≥ 0.70
Outperforms the Groq zero-shot baseline
Correctly distinguishes Analysis and Hot Take at least 70% of the time

A model achieving these thresholds would provide useful automated categorization of NBA discussions.

7. AI Tool Plan
Label Stress Testing

ChatGPT will be used to generate ambiguous NBA posts that sit between Analysis and Hot Take labels. These examples will help refine label definitions before annotation begins.

Annotation Assistance

ChatGPT may be used to suggest preliminary labels. All labels will be manually reviewed and corrected by the author before inclusion in the final dataset.

Failure Analysis

After evaluation, incorrect predictions will be analyzed using ChatGPT to identify recurring error patterns. Any identified patterns will be manually verified against the dataset before reporting.