#!/bin/sh

import codecs
from sklc import data
from sklc import model
from sklearn import feature_selection
import sys


TEMP_DIR = 'tmp/model/'

# The pathname to the generated suggestion data.
DATA_PATH = 'tmp/suggestion.top.10.txt'

# The pathname to the ground truth error list.
GT_PATH = 'data/error.gt.txt'

# The splitting ratio of the training, validation and testing set.
TRAIN_SPLITS_RATIO = 0.8
TEST_SPLITS_RATIO  = 0.2

SUFFIX = 'acl.top10'


# The default settings.
DEFAULT_CONFIG = lambda name: \
        (False,                          # override
         True,                           # weighted 
         TEMP_DIR + name + '.' + SUFFIX, # seralization path
         None,                           # customized grid
         None)                           # selector


FEATURES = [
        'DistanceFeature(Levenstein)',
        'StringSimilarityFeature',
        'LanguagePopularityFeature',
        'LexiconExistenceFeature(Lexical)',
        'LexiconExistenceFeature(Wikipedia)',
        'ContextCoherenceFeature(Bigram)',
        'ContextCoherenceFeature(Trigram)',
        'ContextCoherenceFeature(Fourgram)',
        'ContextCoherenceFeature(Fivegram)',
        'ApproximateContextCoherenceFeature(Bigram)',
        'ApproximateContextCoherenceFeature(Trigram)',
        'ApproximateContextCoherenceFeature(Fourgram)',
        'ApproximateContextCoherenceFeature(Fivegram)',
        ]


TRAIN_SETTINGS = [
        (model.RandomForestModel     , DEFAULT_CONFIG('rf')),
        (model.ExtraTreesModel       , DEFAULT_CONFIG('et')),
        (model.AdaBoostModel         , DEFAULT_CONFIG('ab')),
        (model.GradientBoostingModel , DEFAULT_CONFIG('gb')),
        #(model.MLPModel              , DEFAULT_CONFIG('mlp')),
        (model.SupportVectorModel    , DEFAULT_CONFIG('svr')),
       ]


def data_split(data_path, gt_path, train_ratio, test_ratio):
    """ Split data by the position of the original text at the given ratio.
    """
    if train_ratio + test_ratio != 1.0:
        raise ValueError

    # Get the split positions according to the ratios.
    poss = [int(l.split('\t')[0]) for l in codecs.open(gt_path, 'r', 'utf-8')]
    spos = poss[int(len(poss) * train_ratio)]

    # Splits data according to the ratios.
    dataset = data.Dataset.read(DATA_PATH)
    return dataset.subset(filt=lambda e: e.position < spos,
            return_complement=True)


def main():
    train_data, test_data = data_split(DATA_PATH, GT_PATH,
            TRAIN_SPLITS_RATIO, TEST_SPLITS_RATIO)

    for md, (_override, _weighted, _path, _grid, _estimator) in TRAIN_SETTINGS:
        if _grid != None:
            md(train_data, override=_override, weighted=_weighted,
                    pkl_path=_path, para_grid=_grid, features=FEATURES)
        else:
            md(train_data, override=_override, weighted=_weighted,
                    pkl_path=_path, features=FEATURES)

    for md, (_override, _weighted, _path, _grid, _estimator) in TRAIN_SETTINGS:
        print(md)
        
        try:
            lm = md(None, None, train_data, pkl_path=_path) if md == model.SKLModel else md(train_data, pkl_path=_path)
            lm.predict(test_data)
            print('1: {}\n3: {}\n5: {}\n10: {}\nA: {}\n'
                .format(test_data.precision_at(1),
                      test_data.precision_at(1),
                      test_data.precision_at(3),
                      test_data.precision_at(5),
                      test_data.precision_at(10),
                      test_data.precision_at()))
        except Exception:
            pass


if __name__ == '__main__':
    main()
