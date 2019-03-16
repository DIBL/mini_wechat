#!/usr/bin/perl

use Parallel::ForkManager;
my $MAX_PROCESSES = 10;
my $pm = Parallel::ForkManager->new($MAX_PROCESSES);

my $id = 0;

$pm->run_on_start( sub {
    $id += 1;
    printf "%s : Start - Running process: %d\n", scalar localtime, $id;
});

$pm->run_on_finish( sub {
	$id -= 1;
	printf "%s : Shut-down - Running process: %d\n", scalar localtime, $id;
});

my @names = ('shuaiNi', 'yifuSun', 'zhiXu', 'yangTian', 'wenjiLiu', 'shuaiHu', 'fangshiLi', 'feiyiWang', 'ouluXu', 'jingLi');
my $names_len = scalar @names;

for (my $i = 0; $i < $names_len; $i += 1) {
	$pm->start and next;

	system qq{java -jar ./target/mini-wechat-perfTest.jar /Users/Hans/Self-Learning/Project/mini-wechat/perfTestConfig/$names[$i]\.csv};
	
	$pm->finish;
}

$pm->wait_all_children;