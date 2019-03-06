#!/usr/bin/perl

use Parallel::ForkManager;
my $MAX_PROCESSES = 60;
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


my %port = get_port();
my %password = get_password();
my @names = ('Shuai Ni', 'Yifu Sun', 'Zhi Xu', 'Yang Tian', 'Wenji Liu', 'Shuai Hu', 'Fangshi Li', 'Feiyi Wang', 'Oulu Xu', 'Jing Li');
my $names_len = scalar @names;
my $msgCount = 10;



# Launch client server
for (my $i = 0; $i < $names_len; $i += 1) {
	$pm->start and next;

	system qq{java -jar ./target/mini-wechat-client-jar-with-dependencies.jar $port{$names[$i]}};

	$pm->finish;
}

sleep(5);

# Total message sent is $msgCount * Combination(n, 2)
for (my $i = 0; $i < $names_len; $i += 1) {
	for (my $j = $i + 1; $j < $names_len; $j += 1) {
		$pm->start and next;

		system qq{java -jar ./target/mini-wechat-perftest-jar-with-dependencies.jar '$names[$i]' $password{$names[$i]}, $port{$names[$i]} '$names[$j]' $password{$names[$j]}, $port{$names[$j]} $msgCount};
		
		$pm->finish;
	}
}

$pm->wait_all_children;

sub get_port {
	return (
		"Shuai Ni" => 2000,
		"Yifu Sun" => 2500,
		"Zhi Xu" => 3000,
		"Yang Tian" => 3500,
		"Wenji Liu" => 4000,
		"Shuai Hu" => 4500,
		"Fangshi Li" => 5000,
		"Feiyi Wang" => 5500,
		"Oulu Xu" => 6000,
		"Jing Li" => 6500
	);
}

sub get_password {
	return (
		"Shuai Ni" => "shuaini",
		"Yifu Sun" => "yifusun",
		"Zhi Xu" => "zhixu",
		"Yang Tian" => "yangtian",
		"Wenji Liu" => "wenjiliu",
		"Shuai Hu" => "shuaihu",
		"Fangshi Li" => "fangshili",
		"Feiyi Wang" => "feiyiwang",
		"Oulu Xu" => "ouluxu",
		"Jing Li" => "jingli"
	);
}
