import pandas as pd
import numpy as np
import os
import pydotplus
from sklearn import tree
from IPython.display import Image
from sklearn.tree._tree import TREE_LEAF
from IPython.display import display
import graphviz
from graphviz import Source

df = pd.read_csv('data/backup_House.csv', error_bad_lines=False, index_col=False, dtype='unicode')

def print_columns(df):
    count = 0
    for col in df.columns:
        print(str(col) + " Column: " + str(count))
        count += 1

print_columns(df)

# drop columns with text data or unnecessary features that are irrelevant for price
cols = [0,1,6,7,8,9,10,11,12,13,14,15,16,18,19,20,22,23,24,25,26,27,29,30,32,34,35,36,37,38]
df.drop(df.columns[cols], axis=1, inplace=True)

# swap the price column to the end
# col_list = list(df)
# col_list[8], col_list[11] = col_list[11], col_list[8]
# df.columns = col_list
df = df.reindex(columns=['Price','AreaSpace_SQFT','Bathrooms','Bedrooms','Cooling','Parking','YearBuilt','State','Type'])

print(len(df))
# dropping all null values, and after the following operation of the length is
df = df.dropna()
filter = df['AreaSpace_SQFT'] != "No Data"
df = df[filter]
filter = df['Bathrooms'] != "No Data"
df = df[filter]
filter = df['Bedrooms'] != "No Data"
df = df[filter]
filter = df['YearBuilt'] != "No Data"
df = df[filter]

print(len(df))
df = df.drop(df.index[100000:-1])

print(len(df))

# df = pd.get_dummies(df, columns=['AreaSpace_SQFT','Bathrooms','Bedrooms','Cooling','Locality','Parking','YearBuilt','State','Type'])
df = pd.get_dummies(df, columns=['Cooling','Parking','State','Type'])
# print_columns(df)


# def prune_index(inner_tree, index, threshold):
#     if inner_tree.value[index].min() < threshold:
#         # turn node into a leaf by "unlinking" its children
#         inner_tree.children_left[index] = TREE_LEAF
#         inner_tree.children_right[index] = TREE_LEAF
#     # if there are shildren, visit them as well
#     if inner_tree.children_left[index] != TREE_LEAF:
#         prune_index(inner_tree, inner_tree.children_left[index], threshold)
#         prune_index(inner_tree, inner_tree.children_right[index], threshold)


y = df.pop('Price')
X = df
# print(y)

#Train test split of model
from sklearn.model_selection import train_test_split
# X_train,X_test,y_train,y_test = train_test_split(X,y,test_size = 0.1,random_state = 0)
X_train,X_test,y_train,y_test = train_test_split(X,y,test_size=0.2,random_state = 0)
# print("train")
# print(X_train)
# print("val_train")
# print(y_train)

var_classifier = tree.DecisionTreeClassifier(max_depth = 3)
train_classifier = var_classifier.fit(X_train, y_train)

print(dict(zip(df.columns, train_classifier.feature_importances_)))

# print(tree.export_graphviz(train_classifier, None))

# print("total sum is: "+str(sum(train_classifier.tree_.children_right < 0)))
# prune_index(train_classifier.tree_, 0, 1)
# print("total sum is: "+str(sum(train_classifier.tree_.children_right < 0)))

graph = graphviz.Source(tree.export_graphviz(train_classifier, feature_names=list(df.columns.values), rounded=True, filled=True))
graph.format = "png"
graph.render("tree")

var_prediction = train_classifier.predict(X_test)
# print(var_prediction)

from sklearn.metrics import precision_recall_fscore_support as score
precision, recall, fscore, support = score(y_test, var_prediction, zero_division=1)

print('precision: {}'.format(precision))
print('recall: {}'.format(recall))
print('fscore: {}'.format(fscore))
print('support: {}'.format(support))

print("Precision :")
print(sum(precision) / len(precision) )

